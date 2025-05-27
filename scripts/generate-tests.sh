#!/bin/bash
# set -e

# Configuration
BASE_SHA=$1
HEAD_SHA=$2

echo "API key length: ${#OPENAI_API_KEY}"
if [ -z "$OPENAI_API_KEY" ]; then
    echo "Error: OPENAI_API_KEY is not set."
    exit 1
fi
MODEL="gpt-4.1-nano"
TEST_DIR="src/test/java"
# Get modified Java files (excluding test files)
files=$(git diff --name-only $BASE_SHA $HEAD_SHA -- '*.java' | grep -v "$TEST_DIR")
echo $(pwd)
echo 'Files: ' $files
mkdir -p $TEST_DIR/generated

for file in $files; do
    echo "Processing $file"

    # Extract class content
    class_content=$(cat "$file")

    # Generate test via OpenAI API
    response=$(curl -s -v https://api.openai.com/v1/chat/completions \
      -H "Authorization: Bearer $OPENAI_API_KEY" \
      -H "Content-Type: application/json" \
      -d @- <<EOF
{
  "model": "$MODEL",
  "messages": [
    {
      "role": "system",
      "content": "You are a Java developer who writes complete and valid JUnit 5 tests."
    },
    {
      "role": "user",
      "content": "Write JUnit 5 test cases for the following class:\n\n$class_content"
    }
  ],
  "temperature": 0.3
}
EOF
    )

    # Extract the code block from response (assuming Markdown-style output)
    test_code=$(echo "$response" | jq -r '.choices[0].message.content' | sed -n '/```java/,/```/p' | sed '1d;$d')

    # Fallback if no code block
    if [ -z "$test_code" ]; then
        echo "No test generated for $file"
        continue
    fi

    # Save generated test file
    base_name=$(basename "$file" .java)
    echo "$test_code" > "$TEST_DIR/generated/${base_name}Test.java"
    echo "Generated test saved to $TEST_DIR/generated/${base_name}Test.java"
done
