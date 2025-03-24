window.AudioContext = window.AudioContext || window.webkitAudioContext;
let ctx;
const startButton = document.querySelector('body');
const audioBuffers = new Map(); // Store AudioBuffers for each note
const oscillators = new Map();  // Track active oscillators (notes currently being played)
const oscillatorsPressed = new Map(); // Track notes physically pressed
const sustainedNotes = new Set(); // Track sustained notes (notes held with sustain pedal)
let sustainOn = false; // Track sustain pedal state
let midiConected = false; //Track midi conection
let audioLoaded = false; // Track if all audio is loaded
const audioType = "wav";
const pathToPianoAudio = "/piano-pro/rec/piano-audio";
const pianoNotes = [
    "A0", "Bb0", "B0", "C1", "Db1", "D1", "Eb1", "E1", "F1", "Gb1", "G1", "Ab1",
    "A1", "Bb1", "B1", "C2", "Db2", "D2", "Eb2", "E2", "F2", "Gb2", "G2", "Ab2",
    "A2", "Bb2", "B2", "C3", "Db3", "D3", "Eb3", "E3", "F3", "Gb3", "G3", "Ab3",
    "A3", "Bb3", "B3", "C4", "Db4", "D4", "Eb4", "E4", "F4", "Gb4", "G4", "Ab4",
    "A4", "Bb4", "B4", "C5", "Db5", "D5", "Eb5", "E5", "F5", "Gb5", "G5", "Ab5",
    "A5", "Bb5", "B5", "C6", "Db6", "D6", "Eb6", "E6", "F6", "Gb6", "G6", "Ab6",
    "A6", "Bb6", "B6", "C7", "Db7", "D7", "Eb7", "E7", "F7", "Gb7", "G7", "Ab7",
    "A7", "Bb7", "B7", "C8"
];

// Track which keys are visually pressed by mouse and MIDI separately
const mousePressedKeys = new Set();
const midiPressedKeys = new Set();

// Function to update visual state of a key
function updateKeyVisualState(noteName, isPressed, source) {
    // Find the key element by its MIDI note number
    const midiNote = pianoNotes.indexOf(noteName) + 21;
    const key = document.querySelector(`[onclick*="${midiNote}"]`);
    if (key) {
        if (source === 'mouse') {
            if (isPressed) {
                mousePressedKeys.add(noteName);
            } else {
                mousePressedKeys.delete(noteName);
            }
        } else if (source === 'midi') {
            if (isPressed) {
                midiPressedKeys.add(noteName);
            } else {
                midiPressedKeys.delete(noteName);
            }
        }

        // Key should be blue if either:
        // 1. MIDI key is pressed (physical piano)
        // 2. Mouse is down AND over this specific key
        if (midiPressedKeys.has(noteName)) {
            key.classList.add('blue');
        } else if (mousePressedKeys.has(noteName) && key.matches(':hover')) {
            key.classList.add('blue');
        } else {
            key.classList.remove('blue');
        }
    }
}

// Function to handle mouse down on a key
function handleKeyMouseDown(event) {
    const key = event.target;
    const onclick = key.getAttribute('onclick');
    if (onclick) {
        const midiNote = onclick.match(/'(\d+)'/)[1];
        const noteName = pianoNotes[parseInt(midiNote) - 21];
        if (!mousePressedKeys.has(noteName)) {
            updateKeyVisualState(noteName, true, 'mouse');
            noteOn(parseInt(midiNote), 127);
        }
    }
}

// Function to handle mouse up on a key
function handleKeyMouseUp(event) {
    const key = event.target;
    const onclick = key.getAttribute('onclick');
    if (onclick) {
        const midiNote = onclick.match(/'(\d+)'/)[1];
        const noteName = pianoNotes[parseInt(midiNote) - 21];
        updateKeyVisualState(noteName, false, 'mouse');
        noteOff(parseInt(midiNote));
    }
}

// Function to handle mouse leave on a key
function handleKeyMouseLeave(event) {
    const key = event.target;
    const onclick = key.getAttribute('onclick');
    if (onclick) {
        const midiNote = onclick.match(/'(\d+)'/)[1];
        const noteName = pianoNotes[parseInt(midiNote) - 21];
        updateKeyVisualState(noteName, false, 'mouse');
        noteOff(parseInt(midiNote));
    }
}

// Add event listeners to piano keys
function addPianoKeyEventListeners() {
    const pianoKeys = document.querySelectorAll('polygon');
    pianoKeys.forEach(key => {
        key.addEventListener('mousedown', handleKeyMouseDown);
        key.addEventListener('mouseup', handleKeyMouseUp);
        key.addEventListener('mouseleave', handleKeyMouseLeave);
        // Add mouse move event to update visual state when mouse moves over keys
        key.addEventListener('mousemove', (event) => {
            const onclick = key.getAttribute('onclick');
            if (onclick) {
                const midiNote = onclick.match(/'(\d+)'/)[1];
                const noteName = pianoNotes[parseInt(midiNote) - 21];
                updateKeyVisualState(noteName, mousePressedKeys.has(noteName), 'mouse');
            }
        });
    });
}

//load all audio files
async function loadAudio() {
    if(!ctx) {
        console.error('Audio Context not initialized. Please ensure you click to start audio context first.');
        return;
    }
    
    if (audioLoaded) {
        //console.log('Audio files already loaded, skipping...');
        return;
    }
    
    console.log('Starting to load audio files...');
    const loadPromises = [];
    const loadedNotes = new Set(); // Track which notes we've already loaded
    
    for(let note = 0; note < 88; note++) {
        const noteName = pianoNotes[note];
        
        // Skip if we've already loaded this note
        if (loadedNotes.has(noteName)) {
            //console.log(`Skipping already loaded note: ${noteName}`);
            continue;
        }
        
        const audioPath = `${pathToPianoAudio}/${noteName}.${audioType}`;
        //console.log(`Attempting to load audio file: ${audioPath}`);
        
        const loadPromise = fetch(audioPath)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}. File: ${audioPath}`);
                }
                return response.arrayBuffer();
            })
            .then(data => {
                if (!data || data.byteLength === 0) {
                    throw new Error(`Empty audio data received for ${noteName}`);
                }
                return new Promise((resolve, reject) => {
                    ctx.decodeAudioData(data, 
                        (buffer) => {
                            audioBuffers.set(noteName, buffer);
                            loadedNotes.add(noteName);
                            //console.log(`Successfully loaded audio for note: ${noteName}`);
                            resolve();
                        },
                        (error) => {
                            console.error(`Error decoding audio data for ${noteName}:`, error);
                            reject(error);
                        }
                    );
                });
            })
            .catch(err => {
                console.error(`Failed to load audio for ${noteName}:`, err);
                throw err;
            });
        loadPromises.push(loadPromise);
    }

    try {
        await Promise.all(loadPromises);
        audioLoaded = true;
        console.log(`Successfully loaded ${audioBuffers.size} audio files`);
        console.log('Available notes:', Array.from(audioBuffers.keys()).join(', '));
    } catch (error) {
        console.error('Failed to load some audio files. Piano may not work correctly:', error);
    }
}

// Event listener for the start button
startButton.addEventListener('click', async () => {
    try {
        // Initialize the AudioContext when the button is clicked
        ctx = new AudioContext();
        startButton.disabled = true;
        await ctx.resume(); // Wait for AudioContext to resume

        console.log('AudioContext initialized and resumed');

        // Load all audio files immediately
        await loadAudio();

        if (navigator.requestMIDIAccess) {
            navigator.requestMIDIAccess().then(success, failure);
        }
    } catch (error) {
        console.error('Error during initialization:', error);
    }
});

// Success callback for MIDI access
function success(midiAccess) {
    if(midiConected) return;
    midiConected = true;
    midiAccess.addEventListener('statechange', updateDevices);

    const inputs = midiAccess.inputs;
    inputs.forEach(input => {
        input.addEventListener('midimessage', handleInput);
    });

    // Add event listeners to piano keys after MIDI connection
    addPianoKeyEventListeners();
}

// Handle incoming MIDI messages
function handleInput(input) {
    const [command, note, velocity] = input.data;

    switch (command) {
        case 144: // noteOn
            velocity > 0 ? noteOn(note, velocity) : noteOff(note);
            break;
        case 128: // noteOff
            noteOff(note);
            break;
        case 176: // sustain pedal
            handleSustainPedal(velocity);
            break;
    }
}

// Start note by playing the corresponding audio buffer
function noteOn(note, velocity) {
    if (!ctx) {
        alert('Please click anywhere on the page to initialize the audio system.');
        return;
    }
    
    if (!audioLoaded) {
        alert('Please wait while the piano sounds are loading. This may take a few seconds.');
        return;
    }

    // Convert MIDI note number to pianoNotes array index (MIDI notes start at 21 for A0)
    const noteIndex = note - 21;
    if (noteIndex < 0 || noteIndex >= pianoNotes.length) {
        console.error(`Invalid MIDI note number: ${note}. Must be between 21 and 108.`);
        return;
    }

    const noteName = pianoNotes[noteIndex];

    // Update visual state for MIDI input
    updateKeyVisualState(noteName, true, 'midi');

    // If the note is already playing, stop it first (this handles retriggering the same note)
    if (oscillators.has(noteName)) {
        noteOff(noteName); // Stop the previous instance of the note immediately
    }

    // Play the note since we know the buffer is already loaded
    playNoteBuffer(noteName, velocity);
}

// Function to play the note using AudioBufferSourceNode
function playNoteBuffer(noteName, velocity) {
    if (!ctx) {
        console.error('Audio Context not initialized for playback');
        return;
    }

    const audioBuffer = audioBuffers.get(noteName);
    if (!audioBuffer) {
        console.error(`No audio buffer found for note: ${noteName}`);
        console.log('Available buffers:', Array.from(audioBuffers.keys()));
        return;
    }

    try {
        // Create a new GainNode to control the volume
        const gainNode = ctx.createGain();
        const volume = Math.min(velocity / 60, 1);
        gainNode.gain.setValueAtTime(volume, ctx.currentTime);
        gainNode.connect(ctx.destination);

        // Create a new AudioBufferSourceNode
        const source = ctx.createBufferSource();
        source.buffer = audioBuffer;
        source.connect(gainNode);

        // Play the audio
        source.start(0);

        source.onended = () => {
            oscillators.delete(noteName);
            if (sustainOn) {
                sustainedNotes.delete(noteName);
            }
        };

        // Track the currently playing note
        oscillators.set(noteName, { source, gainNode });
        oscillatorsPressed.set(noteName, source);

        if (sustainOn) {
            sustainedNotes.add(noteName);
        }
    } catch (error) {
        console.error(`Error playing note ${noteName}:`, error);
    }
}

// Stop note with volume fade-out or immediately if sustain pedal is off
function noteOff(note) {
    // Convert MIDI note number to note name if necessary
    const noteName = typeof note === 'number' ? pianoNotes[note - 21] : note;
    
    // Update visual state for MIDI input
    updateKeyVisualState(noteName, false, 'midi');
    
    if (oscillators.has(noteName)) {
        const { source, gainNode } = oscillators.get(noteName);
        oscillatorsPressed.delete(noteName);

        // If sustain pedal is off, stop the note immediately with a fade-out
        if (!sustainOn) {
            const fadeOutTime = 0.05; // Adjust fade-out duration (seconds)
            const interval = 20; // Adjust interval for smoother fade
            let currentVolume = gainNode.gain.value;
            const fadeOutInterval = setInterval(() => {
                currentVolume -= currentVolume / (fadeOutTime * (1000 / interval));
                gainNode.gain.setValueAtTime(Math.max(0, currentVolume), ctx.currentTime);
                if (currentVolume <= 0.001) {
                    clearInterval(fadeOutInterval);
                    source.stop(); // Stop the audio buffer playback
                    oscillators.delete(noteName);
                }
            }, interval);
        } else {
            // If sustain pedal is on, add to sustained notes but don't stop yet
            sustainedNotes.add(noteName);
        }
    } else if (sustainedNotes.has(noteName)) {
        // If the note is in sustained notes, ensure we stop it correctly when the pedal is off
        sustainedNotes.delete(noteName);
    }
}

// Handle sustain pedal press and release
function handleSustainPedal(velocity) {
    sustainOn = velocity > 0; // If velocity is greater than 0, pedal is pressed

    // If pedal is released, stop all non-pressed notes that are sustained
    if (!sustainOn) {
        sustainedNotes.forEach(note => {
            if (!oscillatorsPressed.has(note)) {
                noteOff(note); // Stop non-pressed sustained notes
            }
        });
    }
}

// Update MIDI devices when their state changes
function updateDevices(event) {
    // Optional: You can log or process device updates here
}

// Failure callback for MIDI access
function failure() {
    console.log('Could not connect to MIDI');
}

function check(id, key) {
    const noteName = pianoNotes[parseInt(id) - 21];
    updateKeyVisualState(noteName, true, 'mouse');
    noteOn(id, 127);
    execute(key, noteName);
}

async function execute(theKey, noteName) {
    await wait(90); // Wait
    updateKeyVisualState(noteName, false, 'mouse');
    noteOff(parseInt(theKey.getAttribute('onclick').match(/'(\d+)'/)[1]));
}