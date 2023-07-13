cmake_minimum_required(VERSION 3.4.1)

project(crash-lib)

add_library(crash-lib SHARED jni/native-lib.cpp)
