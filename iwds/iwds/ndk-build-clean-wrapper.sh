#!/bin/bash

function echoc() {
	echo -e "\e[0;91m$1\e[0m"
}

echoc "invoke ndk-build"
cd jni
ndk-build clean || exit 1

if [ ! -d "../../iwds-jar/libs" ]; then
	exit 0
fi

cd ../../iwds-jar/libs/

target_files=("libsafeparcel.so" "libslpt-linux.so")

for file in ${target_files[@]}; do
	file_list=`find . -name $file`
	if [ $? -ne 0 ]; then
		echoc "Unable to exec find command. Abort!!!!!"
	        exit 1
	fi

	if [ -z "$file_list" ]; then
        	continue
	fi

	file_array=(${file_list// /})

	for item in ${file_array[@]}
	do
		if [ -f $item ]; then
			rm -vf $item
			if [ $? -ne 0 ]; then
				echoc "Unable to rm $PWD/$item. Abort!!!!!"
				exit 1
			fi
		fi
	done

	echoc "=============== $file remove done ==============="
done
