#!/bin/bash

[[ "$MP" == "" ]] && MP=5

function echoc() {
	echo -e "\e[0;91m$1\e[0m"
}

echoc "invoke ndk-build -j$MP"
cd jni
$NDK_HOME/ndk-build -j$MP || exit 1

cd ..
if [ ! -d libs ]; then
	echoc "Unable to find $PWD/libs. Abort!!!!!"
	exit 1
fi

cd libs

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
		file_dir=`dirname $item`

		target_dir=../../iwds-jar/libs/$file_dir
		if [ ! -d "target_dir" ]; then
			mkdir -vp $target_dir
			if [ $? -ne 0 ]; then
				echoc "Unable to mkdir at $target_file. Abort!!!!!"
				exit 1
			fi
		fi

		if [ -f "$target_dir/$file" ]; then
			rm -vf $target_dir/$file
		fi

		mv -vf $item $target_dir/$file
		if [ $? -ne 0 ]; then
			echoc "Unable to mv $PWD/$item to $target_dir. Abort!!!!!"
			exit 1
		fi
	done

	echoc "=============== $file copy done ==============="
done
