Tests for `rtpstalk` library.

# Prereqs

- Following network interfaces available in the system: lo, eth0
- Fast-DDS v2.1.1
- compiled `rtpstalk` version of HelloWorldExample

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
