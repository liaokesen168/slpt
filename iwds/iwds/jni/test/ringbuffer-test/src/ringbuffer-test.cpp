//============================================================================
// Name        : ringbuffer-test.cpp
// Author      : 
// Version     :
// Copyright   : Your copyright notice
// Description : Hello World in C++, Ansi-style
//============================================================================

#include "ringbuffer.h"

#include <iostream>
#include <string.h>

using namespace std;

int main() {
    Iwds::RingBuffer buffer(10);

    cout << "size: " << buffer.size() << endl;

    buffer.put("abcd", 5);
    cout << "size: " << buffer.size() << endl;

    char temp[20];
    buffer.get(temp, 5);
    cout << "temp: " << temp << endl;
    cout << "size: " << buffer.size() << endl;

    //full size
    memset(temp, 0, 20);
    buffer.put("0123456789",10);
    cout << "size: " << buffer.size() << endl;
    buffer.get(temp, 5);
    cout << "temp: " << temp << endl;
    cout << "size: " << buffer.size() << endl;

    //reallocate
    memset(temp, 0, 20);
    buffer.put("0123456789abcdef", 16);
    cout << "size: " << buffer.size() << endl;

    //Overlapped
    cout << "overlapped" << endl;
    buffer.put("overlapped", sizeof("overlapped") - 1);
    buffer.get(temp, 4);
    cout << "temp: " << temp << endl;
    cout << "size: " << buffer.size() << endl;
    buffer.get(temp, 4);
    cout << "temp: " << temp << endl;
    cout << "size: " << buffer.size() << endl;
    buffer.get(temp, buffer.size());
    cout << "temp: " << temp << endl;
    cout << "size: " << buffer.size() << endl;

    buffer.put("this is test", sizeof("this is test") - 1);
    cout << "size: " << buffer.size() << endl;
    buffer.get(temp, buffer.size());
    cout << "temp: " << temp << endl;
    cout << "size: " << buffer.size() << endl;

	return 0;
}
