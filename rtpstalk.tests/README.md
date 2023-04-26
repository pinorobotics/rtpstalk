Tests for `rtpstalk` library.

# Prereqs

- Following network interfaces available in the system: lo, eth0
- Fast-DDS v2.1.1
- compiled `rtpstalk` version of HelloWorldExample

# Linux setup

## Fast-RTPS

### Build Fast-RTPS

```bash
git clone https://github.com/eProsima/Fast-DDS.git
cd Fast-DDS/
git checkout v2.1.1
mkdir build_v2.1.1
cd build_v2.1.1/
cmake -DTHIRDPARTY=ON -DCOMPILE_EXAMPLES=ON ..
make
DESTDIR=$(pwd)/install make install
```

### Compiling HelloWorldExample

``` bash
cd rtpstalk.tests
mkdir -p bld/fastdds
cd bld/fastdds
cmake -DCMAKE_PREFIX_PATH=<PATH TO Fast-RTPS>/build/install/usr/local/share/fastrtps/cmake ../../src/test/cpp/fastdds
make
```

## CycloneDDS

### Build CycloneDDS

- No debug support
``` bash
apt install ros-humble-rmw-cyclonedds-cpp
```
- With debug support
``` bash
git clone https://github.com/eclipse-cyclonedds/cyclonedds.git
git checkout releases/0.10.x
mkdir build_v0.10.x
cd build_v0.10.x/
cmake -DCMAKE_INSTALL_PREFIX=`pwd`/install ..
cmake --build .
```

### Compiling HelloWorldExample

``` bash
mkdir -p bld/cyclonedds
cd bld/cyclonedds
cmake -DCMAKE_PREFIX_PATH=<PATH TO CYCLONEDDS>/build_v0.10.x/install/lib/cmake ../../src/test/cpp/cyclonedds/
make
```

#### Troubleshooting:

##### undefined symbol: DDS_XTypes_TypeInformation_desc
If compilation fails with:
``` 
../../../bin/idlc: symbol lookup error: ../../../bin/idlc: undefined symbol: DDS_XTypes_TypeInformation_desc
```
Make sure that no other cyclonedds is installed:

``` bash
sudo apt purge cyclonedds-tools
sudo apt purge ros-humble-cyclonedds
```

### Enable logging

Create /home/ubuntu/cdds.xml:

``` xml
<?xml version="1.0" encoding="utf-8"?>
 <!-- export CYCLONEDDS_URI="file:///home/ubuntu/cdds.xml" -->
 <CycloneDDS
   xmlns="https://cdds.io/config"
   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
   xsi:schemaLocation="https://cdds.io/config https://raw.githubusercontent.com/eclipse-cyclonedds/cyclonedds/master/etc/cyclonedds.xsd">
   <Domain Id="any">
     <General>
       <Interfaces>
        <NetworkInterface autodetermine="true" priority="default" multicast="default" />
      </Interfaces>
      <AllowMulticast>default</AllowMulticast>
      <MaxMessageSize>65500B</MaxMessageSize>
    </General>
    <Tracing>
      <Verbosity>finest</Verbosity>
      <OutputFile>
        /home/ubuntu/cdds.log
      </OutputFile>
    </Tracing>
  </Domain>
</CycloneDDS>
```

# Windows setup

Run [build-tools-for-visual-studio-2022 installer](https://visualstudio.microsoft.com/downloads/#build-tools-for-visual-studio-2022):
- Open Individual Components and select:
    - C++ Cmake tools for Windows (includes MSVC v143 - VS 2022 C++ x64/x86 build tools)
    - Windows 10 SDK

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
