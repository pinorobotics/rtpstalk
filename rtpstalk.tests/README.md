Tests for `rtpstalk` library.

# Prereqs

- Following network interfaces available in the system: lo, eth0
- Fast-DDS v2.1.1
- compiled `rtpstalk` version of HelloWorldExample

# Linux setup

## Setup Fast-RTPS

Build Fast-RTPS:

```bash
git clone https://github.com/eProsima/Fast-DDS.git
cd Fast-DDS/
git checkout v2.1.1
mkdir build
cd build/
cmake -DTHIRDPARTY=ON -DCOMPILE_EXAMPLES=ON ..
make
DESTDIR=$(pwd)/install make install
```

## Compiling HelloWorldExample

``` bash
cd rtpstalk.tests
mkdir bld
cd bld
cmake -DCMAKE_PREFIX_PATH=<PATH TO Fast-RTPS>/build/install/usr/local/share/fastrtps/cmake ../src/test/cpp/
make
```

# Windows setup

Run [build-tools-for-visual-studio-2022 installer](https://visualstudio.microsoft.com/downloads/#build-tools-for-visual-studio-2022):
- Open Individual Components
- Select:
-- C++ Cmake tools for Windows (includes MSVC v143 - VS 2022 C++ x64/x86 build tools)
-- Windows 10 SDK

Install [fast-dds-2-6-0](https://www.eprosima.com/index.php/component/ars/repository/eprosima-fast-dds/eprosima-fast-dds-2-6-0/eprosima_fast-dds-2-6-0-windows-exe?format=raw)

## Compiling HelloWorldExample

Open Developer Command Prompt and run following commands:

```
cd rtpstalk.tests
mkdir bld
cd bld
cmake ..\src\test\cpp
msbuild ALL_BUILD.vcxproj
move Debug\HelloWorldExample.exe HelloWorldExample
```
