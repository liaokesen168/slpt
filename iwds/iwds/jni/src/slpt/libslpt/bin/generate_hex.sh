#!/bin/sh

#usage:
#      ./generate_hex.sh u-boot.bin firmware.hex

if [ $# = 2 ]; then
	hexdump -v -e '"0x" 1/4 "%08x" "," "\n"' $1 > $2
else
	echo "usage:"
	echo "$0 u-boot.bin firmware.hex"
fi
