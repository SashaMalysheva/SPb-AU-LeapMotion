LEAP_SDK = c:\Program Files\LeapSDK
LEAP_LIBRARY = "$(LEAP_SDK)\lib\x64\Leap.dll" -Wl,-rpath,"$(LEAP_SDK)\lib\x64"

all: main.cpp
	g++ -Wall -g -I "$(LEAP_SDK)\include" main.cpp -o main $(LEAP_LIBRARY)

.PHONY: clean
clean:
	rm -rf main.exe *.o