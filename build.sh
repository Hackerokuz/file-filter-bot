#!/bin/bash

# Build the project with Maven
echo "Building Discord File Type Monitor Bot..."
mvn clean package

# Check if build was successful
if [ $? -eq 0 ]; then
    echo "Build successful! You can now run the bot with:"
    echo "java -jar target/file-filter-bot-1.0-SNAPSHOT.jar"
else
    echo "Build failed. Please check the errors above."
fi
