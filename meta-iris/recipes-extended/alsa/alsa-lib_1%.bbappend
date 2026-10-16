
# Remove some features to trim size

EXTRA_OECONF += " \
    --disable-aload \
    --disable-seq \
    --disable-ucm \
    --disable-topology \
    --disable-old-symbols \
    --disable-python \
"
