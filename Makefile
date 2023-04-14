HotelCalifornia.jar:
	javac ./noz224/*.java
	mv **/*.class ./
	jar cfmv HotelCalifornia.jar Manifest.txt *.class

clean:
	rm -f *.class
	rm -f HotelCalifornia.jar

run: clean HotelCalifornia.jar
	java -jar HotelCalifornia.jar