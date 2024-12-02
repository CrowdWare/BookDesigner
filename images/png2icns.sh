mkdir bookdesigner.iconset
cp bookdesigner-32.png bookdesigner.iconset/icon_32x32.png
cp bookdesigner-64.png bookdesigner.iconset/icon_64x64.png
cp bookdesigner-128.png bookdesigner.iconset/icon_128x128.png
cp bookdesigner-256.png bookdesigner.iconset/icon_256x256.png
cp bookdesigner-512.png bookdesigner.iconset/icon_512x512.png

iconutil --convert icns bookdesigner.iconset

rm bookdesigner.iconset/*
rmdir bookdesigner.iconset
