// Function to initialize VexFlow
function initializeVexFlow() {
    // Get the rendering context
    const sheetMusic = document.getElementById('sheetMusic');
    
    // Set container styles for centering
    sheetMusic.style.display = 'flex';
    sheetMusic.style.justifyContent = 'center';
    sheetMusic.style.alignItems = 'center';
    
    const renderer = new VexFlow.Renderer(sheetMusic, VexFlow.Renderer.Backends.SVG);

    // Configure the rendering context
    renderer.resize(350, 100);
    const context = renderer.getContext();

    // Create a stave with adjusted position for centering
    const stave = new VexFlow.Stave(25, 0, 300);  // Adjusted Y position to 0 for better centering
    
    // Add a treble clef
    stave.addClef('treble');
    
    // Connect the stave to the rendering context and draw it
    stave.setContext(context).draw();

    // Create a whole note on middle C
    const notes = [
        new VexFlow.StaveNote({ keys: ['c/4'], duration: 'w' })
    ];

    // Create a voice
    const voice = new VexFlow.Voice({ num_beats: 4, beat_value: 4 });
    voice.addTickables(notes);

    // Format and draw
    new VexFlow.Formatter()
        .joinVoices([voice])
        .format([voice], 300);  // Adjusted width to match stave width
    voice.draw(context, stave);
}

// Wait for both fonts to load and the sheetMusic element to be available
document.fonts.ready.then(() => {
    // Check if element already exists
    if (document.getElementById('sheetMusic')) {
        initializeVexFlow();
    } else {
        // Create a MutationObserver to watch for the element
        const observer = new MutationObserver((mutations, obs) => {
            if (document.getElementById('sheetMusic')) {
                initializeVexFlow();
                obs.disconnect(); // Stop observing once element is found
            }
        });

        // Start observing the document with the configured parameters
        observer.observe(document.body, { childList: true, subtree: true });
    }
}); 