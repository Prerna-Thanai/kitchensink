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
MODEL="gpt-4o-mini"
TEST_DIR="src/test/java"
# Get modified Java files (excluding test files)
files=$(git diff --name-only $BASE_SHA $HEAD_SHA -- 'AuthServiceImpl.java' | grep -v "$TEST_DIR")
echo 'Files: ' $files
mkdir -p $TEST_DIR/generated

for file in $files; do
    echo "Processing $file"

    # Extract class content
    class_content=$(cat "$file")
    jq -n \
    --arg model "$MODEL" \
    --arg content "Write JUnit 5 test cases of Spring boot 3+ with mockito for the following class:\n\n$class_content" \
    --argjson temp 0.3 \
    '{
      model: $model,
      messages: [
        { role: "user", content: $content }
      ],
      temperature: $temp
    }' > request.json
    
    # Generate test via OpenAI API
    response=$(curl -s https://api.openai.com/v1/chat/completions \
      -H "Authorization: Bearer $OPENAI_API_KEY" \
      -H "Content-Type: application/json" \
      -H "OpenAI-Organization: $OPENAI_ORG" \
      -H "OpenAI-Project: $OPENAI_PRJ" \
      -d @request.json
    )

    rm request.json
    # Extract the code block from response (assuming Markdown-style output)
    echo "Response: $response"
    test_code=$(echo "$response" | jq -r '.choices[0].message.content' | sed -n '/```java/,/```/p' | sed '1d;$d')

    # Fallback if no code block
    if [ -z "$test_code" ]; then
        echo "No test generated for $file"
        continue
    fi

    # Save generated test file
    package_line=$(grep "^package " "$file")
    package_name=${package_line//package /}
    package_name=${package_name//;/}
    package_dir=$(echo "$package_name" | tr '.' '/')
    output_dir="$TEST_DIR/$package_dir/"
    mkdir -p "$output_dir"
    base_name=$(basename "$file" .java)
    echo "$test_code" > "$output_dir${base_name}AiTest.java"
    echo "Generated test saved to $output_dir${base_name}AiTest.java"
done
