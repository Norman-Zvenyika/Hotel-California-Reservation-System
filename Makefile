noz224.jar:
	javac ./noz224/*.java
	mv **/*.class ./
	jar cfmv noz224.jar Manifest.txt *.class

clean:
	rm -f *.class
	rm -f noz224.jar

run: clean noz224.jar
	java -jar noz224.jar