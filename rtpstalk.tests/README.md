Tests for `rtpstalk` library.

# Prereqs

- following network interfaces available: lo, eth0
- Fast-RTPS 1.9.4 commands

## Setup Fast-RTPS commands

Build Fast-RTPS:

```bash
git clone https://github.com/canonical/Fast-RTPS
cd Fast-RTPS/
git checkout v1.9.4
mkdir build
cd build/
cmake -DTHIRDPARTY=ON -DCOMPILE_EXAMPLES=ON ..
make
```

Export following commands into PATH environment variable:

```
build/examples/C++/HelloWorldExample/HelloWorldExample
```

