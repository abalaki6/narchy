
function MainMenuButton() {
    return $('<div>[@]</div>').addClass('MainMenuButton').click(()=> setTimeout(Menu, 0) );
}

function Menu() {
    const menulayer = div('MenuLayer').attr('oncontextmenu', "return false;");
    const graphCanvas = div('MenuGraph max').appendTo(menulayer);

    let c;

    function close() {
        menulayer.fadeOut(250, ()=> {
            setTimeout(()=>{
                if (c)
                    c.destroy();
                menulayer.remove();
            }, 0);
        });
    }


    const menu = {
        nodes: [ ],
        edges: [ ],

        add: function(item, parent, options) {
            options = options || {};
            options.id = item;

            //disable label if widget is provided
            options.label = options.widget ? "" : item;

            this.nodes.push( { data: options } );
            if (parent)
                this.edges.push( { data: { source: parent, target: item } } );
        }
    };

    const body = $('body');

    const _return = 'return';

    menu.add(_return, null, {
        widget: $('<button>[x]</button>').css({ opacity: 0.75, border: 0 }).click(function(e) {
            console.log(e);
            close();

        })
    });
    menu.add('System', _return, {});
    menu.add('Network', 'System', {});
    menu.add('Memory', 'System', {});
    {
        menu.add('Concepts', 'Memory', {});
        menu.add('Goals', 'Memory', {});
    }
    menu.add('CPU', 'System', {});
    {
        menu.add('Restart', 'System', {});
        menu.add('Shutdown', 'System', {});
    }

    menu.add('Status', _return, {});
    {

        menu.add('Weather', 'Status', {
            widget: $('<iframe width="400" height="400" src="http://wunderground.org"></iframe>')
        });

        {
            menu.add('Temperature', 'Weather', {});
            menu.add('Precipitation', 'Weather', {});
            menu.add('Humidity', 'Weather', {});
            menu.add('Air Quality', 'Weather', {});
        }

        menu.add('Bio', 'Status', {});
        {
            menu.add('Food', 'Bio', {});
            menu.add('Medicine', 'Bio', {});
            menu.add('Shelter', 'Bio', {});
            menu.add('Defense', 'Bio', {});
        }

        menu.add('News', 'Status', {});
    }

    menu.add('Find', _return, {
        //widget: $('<div><input type="text" placeholder="?"></input></div>')
        widget: $('<textarea rows="12" cols="50">At w3schools.com you will learn how to make a website. We offer free tutorials in all web development technologies.</textarea>')
    });



    c = cytoscape({
        container: graphCanvas,

        layout: {
            name: 'concentric',
            concentric: function (node) { // returns numeric value for each node, placing higher nodes in levels towards the centre

                const aStar = node.cy().elements().aStar({root: "#" + _return, goal: node});

                return 100 - (aStar.distance);
            },

            levelWidth: function(n) { return 1; }, // the variation of concentric values in each level

            //equidistant: true,
            minNodeSpacing: 30,

            sweep: Math.PI*2.0
        },

//            layout:
//                //name: 'breadthfirst',
//                //name: 'cose',
//                fit: true
//            },

        ready: (a)=>{


            const cc = a.cy;

            const pr = cc.elements()
                .closenessCentralityNormalized();
            //.degreeCentralityNormalized();
            //.pageRank();

            cc.nodes().each((i,n) => {

                const v = //1 / Math.pow(pr.rank(n), 2);
                    pr.closeness(n);

                n.style({
                    width:  v * 320,
                    height: v * 240
                });

            });

        },

        style: [
            {
                selector: 'node',
                style: {
                    'background-color': '#888',
                    'label': 'data(label)',
                    'text-color': 'white',
                    'shape': 'hexagon',
                    //'width':
                    //  node => 48 * (1+Math.sqrt(node.outdegree())),
                    //'height': node => 30 * (1+Math.sqrt(node.outdegree())),
                    'text-valign': 'center',
                    'text-halign': 'center',
                    'color': '#fff',
                    'font-family': 'Monospace'

                }
            }
        ],

        elements: menu,
    });

    const activeWidgets = new Map();

    function onAdd(node) {

        //console.log(node);

        const data = node._private.data;
        if (data.widget) {

            const widget = (data.widget.jquery) ? data.widget[0] : data.widget; //HACK un-querify

            const nid = node.id();
            widget.setAttribute('id', 'node_' + nid);

            const style = widget.style;
            style.position = 'fixed';
            style.transformOrigin = '0 0';

            menulayer.append(widget);
            activeWidgets.set(nid, node);

            setTimeout(()=> updateWidget(node) , 0)


        }
    }

    c.nodes().each((i,v)=>onAdd(v));
    c.on('add', /* select unselect  */ function (e) {
        onAdd(e.cyTarget);
    });

    c.on('remove', /* select unselect  */ function (e) {

        const node = e.cyTarget;
        const data = node._private.data;
        if (data.widget) {
            const widget = (data.widget[0]) ? data.widget[0] : data.widget; //HACK un-querify
            activeWidgets.remove(node.id());
            widget.detach();
        }

    });

    function updateAll() {

        fastdom.mutate(()=>{
            activeWidgets.forEach((node, nid) => {
                updateWidget(node);
            });
        });
    }

    c.on('pan zoom ready', /* select unselect  */ function (e) {
        updateAll();
    });

    c.on('position style data', /* select unselect  */ function (e) {

        const node = e.cyTarget;
        const data = node._private.data;
        if (data && data.widget) {
            updateWidget(node);
            //console.log(this, that, target);
            //that.commit();
        }
    });


    function zoomTo(ele, zoomDuration) {
        // var pos;
        // if (!ele || !ele.position)
        //     pos = { x: 0, y: 0 };
        // else
        //     pos = ele.position();

        c.animate({
            fit: {
                eles: ele,
                padding: 40
            }
        }, {
            duration: zoomDuration
            /*step: function() {
             }*/
        });
    }

    //--------------
    //right-click autozoom:
    c.on('cxttapstart', function(e) {
        let target = e.cyTarget;
        zoomTo(!target ? undefined : target, 128 /* ms */)
    });

//            const layout = c.makeLayout({
//                /* https://github.com/cytoscape/cytoscape.js-spread */
//                name: 'spread',
//                    minDist: 250,
//                    //padding: 100,
//
//                    speed: 0.06,
//                    animate: false,
//                    randomize: true, // uses random initial node positions on true
//                    fit: false,
//                    maxFruchtermanReingoldIterations: 1, // Maximum number of initial force-directed iterations
//                    maxExpandIterations: 2, // Maximum number of expanding iterations
//
//                    ready: function () {
//                    //console.log('starting spread', Date.now());
//                },
//                stop: function () {
//                    //console.log('stop spread', Date.now());
//                }
//            });
//            c.onRender(()=>{
//               layout.run();
//            });



    menulayer.hide();

    fastdom.mutate(()=>{
        menulayer.appendTo(body);
        menulayer.fadeIn();
    });


}

$(document).ready(() => {

    const io = NARTerminal();
    window.io = io;

    const layout = new GoldenLayout({
        content: [{
            type: 'row',
            content: [
                {
                    type: 'column',
                    content: [{
                        type: 'component',
                        componentName: 'graph',
                        componentState: {}
                    }, {
                        type: 'component',
                        componentName: 'top',
                        componentState: {}
                    }]
                },
                {
                    type: 'column',
                    content: [{
                        type: 'component',
                        componentName: 'options',
                        componentState: {}
                    }, {
                        type: 'component',
                        componentName: 'input',
                        componentState: {}
                    }]
                }
                /*{
                 type: 'component',
                 componentName: 'terminal',
                 componentState: { label: 'A' }
                 }*/
            ]
        }]
    }, $('body') );

    layout.on( 'stackCreated', function( stack ) {

        /*
         * Accessing the DOM element that contains the popout, maximise and * close icon
         */
        stack.header.controlsContainer.append(MainMenuButton());
    });
    layout.registerComponent( 'terminal', function( tgt, state ){
        tgt.getElement().html( IO(io) );
    });
    layout.registerComponent( 'input', function( tgt, state ){
        tgt.getElement().html(
            NALEditor(io).attr('id', 'input')
        );
    });
    layout.registerComponent( 'options', function(tgt, state) {

        // Set default options
        //console.log(JSONEditor());
        //JSONEditor().defaults.options.theme = 'jqueryui';

        // Initialize the editor
        const dd = div('max');
        let editor = new JSONEditor(dd[0], {
            schema: {
                type: "object",
                properties: {
                    name: {"type": "string"}
                }
            }
        });

//            // Set the value
//            editor.setValue({
//                name: "John Smith"
//            });
//
//            // Get the value
//            var data = editor.getValue();
//
//            // Validate
//            var errors = editor.validate();
//            if(errors.length) {
//                // Not valid
//            }
//
//            // Listen for changes
//            editor.on("change",  function() {
//                // Do something...
//            });

        tgt.getElement().html( dd );

    });
    layout.registerComponent( 'graph', function( tgt, state ){

        const graph = Graph(io);
        tgt.getElement().html( graph );
        tgt.on('resize', ()=>{
            setTimeout(()=>graph.graph.resize(), 0);
        });

    });
    layout.registerComponent( 'top', function( tgt, state ){
        tgt.getElement().html( TopTable("active") );
    });

    layout.init();

});


function Graph(terminal) {


    const d = div('graph max');

    const opt = {


        //additional options and overrides for defaults
    };

    const c = spacegraph(d, opt);

    const colorFunc = function (r, g, b) {

        const R = parseInt(r * 255);
        const G = parseInt(g * 255);
        const B = parseInt(b * 255);

        return "rgb(" + R + "," + G + "," + B + ")";

    };

    let changed = true;

//        c.onRender(()=>{
//           console.log('render');
//           changed = false;
//        });

    const maxNodes = 20;
    const updatePeriodMS = 100;


    const layout = c.makeLayout({
        /* https://github.com/cytoscape/cytoscape.js-spread */
        name: 'spread',
        minDist: 250,
        //padding: 100,

        speed: 0.06,
        animate: false,
        randomize: false, // uses random initial node positions on true
        fit: false,
        maxFruchtermanReingoldIterations: 1, // Maximum number of initial force-directed iterations
        maxExpandIterations: 2, // Maximum number of expanding iterations

        ready: function () {
            //console.log('starting spread', Date.now());
        },
        stop: function () {
            //console.log('stop spread', Date.now());
        }
    });


    setInterval(() => {

        layout.run();

        if (!changed)
            return;

        c.batch(() => {

            const nodes = c.nodes();
            const toRemove = (nodes.size()) - maxNodes;
            if (toRemove > 0) {
                //console.log(nodes.size(), 'oversize');
                const sorted = nodes.sort((a, b) => {
                    //increasing priority
                    return a._private.data.pri - b._private.data.pri;
                });

                for (let i = 0; i < toRemove; i++) {
                    //console.log(sorted[i], 'removed');
                    sorted[i].remove();
                }
                //console.log(nodes.size(), 'current size');
            }


            nodes.each((i, n) => {
                const x = n._private.data; //HACK
                if (x) {

                    const p1 = 1 + x.pri; // * d(x, 'belief');
                    const r = parseInt(24 + 48 * (p1 * p1));
                    n.style({
                        //                       sg.spacegraph.style().selector('node')
                        //                       .style('background-color', function(x) {
                        //                           const belief = 0.25 + 0.75 * d(x, 'belief');
                        //                           const aBelief = 0.25 + 0.75 * Math.abs(belief);
                        //                           const pri = 0.25 + 0.75 * d(x, 'pri');
                        width: r,
                        height: r,
                        shape: 'hexagon',
                        backgroundColor: colorFunc(0.25 + 0.75 * x.pri, x.dur, x.qua)


                    });
                }
            });
        });

        changed = false;

    }, updatePeriodMS);

    terminal.on('message', function (x) {
        const id = x.term + x.punc + x.freq + ';' + x.conf; //HACK for Task's
        x.label = x.term; //HACK

        let existing = c.get(id);

        if (!existing) {

            //add
            c.add({group: "nodes", data: x});

        } else {
            //replace / merge
            existing.data = x;
        }

        changed = true;
    });

    d.graph = c;

    return d;
}

//    function gridCell(contents) {
//        return div('grid-stack-item').append(div('grid-stack-item-content').append(contents));
//    }

function IO(term) {

    return new NARConsole(term, (x) => {


        const label = x.term + x.punc + truthString(x.freq, x.conf);

        //const fontSize = 2 * (1 + parseInt(x.pri * 99.0)) + '%';
        const fontSize = (75 + 8 * Math.sqrt(1 + 10 * parseInt(x.pri * 9.0))) + '%';


        const d = document.createElement('div');
        switch (x.punc) {
            case '.':
                d.className = 'belief';
                break;
            case '?':
                d.className = 'question';
                break;
            case '!':
                d.className = 'goal';
                break;
            case ';':
                d.className = 'command';
                break;
        }
        d.style.opacity = 0.5 + 0.5 * x.dur;
        d.style.fontSize = fontSize;
        d.innerText = label;
        return d;


    }).addClass('terminal');

//
//        ).attr('id', 'console');

    //return c;
}


function truthComponentStr(x) {

    const i = parseInt(Math.round(100 * x));
    if (i == 100)
        return '1.0';
    else if (i == 0)
        return '0.0';
    else
        return i / 100.0;
}

function truthString(f, c) {
    return c ?
        ("%" + truthComponentStr(f) +
        ";" + truthComponentStr(c) + "%") :
        "";
}

function div(cssklass) {
    const d = document.createElement('div');
    if (cssklass) {
        d.className = cssklass;
    }
    return $(d);
}


//            div('max grid-stack').append(
//
//                gridCell([


//                ]),
//
//                //gridCell(div().html('<h1>ABC</h1>')),
//                //gridCell( NALTimeline(term).attr('id', 'graph') ),
//
//                gridCell( TopTable("active") )

//            ).gridstack({
//                cellHeight: 160,
//                cellWidth: 160,
//                verticalMargin: 20,
//                horizontalMargin: 20
//            })

