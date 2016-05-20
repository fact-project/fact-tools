# Hexmap

Drawing the FACT camera using D3.js.

## Welcome to JS hell.
This is how I handle this stuff.
First: install Node.js `brew install node`
Second: install browserify `npm install -g browserify`

If you cloned this repository and want to use it do:

    $> npm install .     
    $> browserify main.js -o bundle.js
    $> open index.html

If you want to use this in another project (like your awesome website showing the FACT camera)
To install hexmap simply add

    "dependencies": {
      "hexmap" : "https://github.com/mackaiver/hexmap"
    }

to your package.json and then use it like this

    var parentID = 'fact_map';
    var size = 550;
    var radius = 5;
    var camera = new hexmap.hexmap(parentID, size, radius);

and then later to update this do

    camera.update(data, duration = 0.5);
