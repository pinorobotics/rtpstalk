Tests for `rtpstalk` library.

# Prereqs

- Following network interfaces available in the system: lo, eth0
- Fast-RTPS 1.9.4
- compiled `rtpstalk` version of HelloWorldExample

## Setup Fast-RTPS

Build Fast-RTPS:

```bash
git clone https://github.com/canonical/Fast-RTPS
cd Fast-RTPS/
git checkout v1.9.4
mkdir build
cd build/
cmake -DTHIRDPARTY=ON ..
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
