# Set the classpath to the location of your compiled Java classes
$CLASSPATH = "target/classes"

# Compile the Java code (assuming Maven project structure)
mvn compile

# Check if compilation was successful
if ($LASTEXITCODE -eq 0) {
  # Run the Java program
  java -cp $CLASSPATH MessageTask
} else {
  Write-Host "Compilation failed. Please check your code."
}