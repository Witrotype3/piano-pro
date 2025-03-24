function windowLoading(){
    try{
        console.log(document.getElementById("loaded"));
        let loading = document.getElementById("loaded");
        loading.setAttribute("id", "loading");
    }catch{

    }
}

function windowLoaded(){
    try{
        let loaded = document.getElementById("loading");
        loaded.setAttribute("id", "loaded");
    }catch{

    }
}

function createHeader(){
    // Fetch the content of piano.html
    fetch('/piano-pro/views/header.html')
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok ' + response.statusText);
            }
            return response.text();
        })
        .then(data => {
            // Inject the content into the div with id 'piano'
            document.getElementById('header').innerHTML = data;
        })
        .catch(error => {
            console.error('There was a problem with the fetch operation:', error);
        });
}

function createHome(){
    // Fetch the content of piano.html
    fetch('/piano-pro/views/home.html')
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok ' + response.statusText);
            }
            return response.text();
        })
        .then(data => {
            // Inject the content into the div with id 'piano'
            document.getElementById('body').innerHTML = data;
        })
        .catch(error => {
            console.error('There was a problem with the fetch operation:', error);
        });
}

function createPianoNotes(){
    // Fetch the content of piano.html
    fetch('/piano-pro/views/piano_notes.html')
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok ' + response.statusText);
            }
            return response.text();
        })
        .then(data => {
            // Inject the content into the div with id 'piano'
            document.getElementById('body').innerHTML = data;
        })
        .catch(error => {
            console.error('There was a problem with the fetch operation:', error);
        });
}

function createPitchPerfect(){
    // Fetch the content of piano.html
    fetch('/piano-pro/views/pitch_perfect.html')
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok ' + response.statusText);
            }
            return response.text();
        })
        .then(data => {
            // Inject the content into the div with id 'piano'
            console.log(data);
            document.getElementById('body').innerHTML = data;
        })
        .catch(error => {
            console.error('There was a problem with the fetch operation:', error);
        });
}

function createTreble(){
    // Fetch the content of piano.html
    fetch('/piano-pro/views/treble.html')
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok ' + response.statusText);
            }
            return response.text();
        })
        .then(data => {
            // Inject the content into the div with id 'piano'
            document.getElementById('body').innerHTML = data;
        })
        .catch(error => {
            console.error('There was a problem with the fetch operation:', error);
        });
}

function createPiano(){
    // Fetch the content of piano.html
    fetch('/piano-pro/views/piano.html')
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok ' + response.statusText);
            }
            return response.text();
        })
        .then(piano => {
            // Inject the content into the div with id 'piano'
            document.getElementById('piano').innerHTML = piano;
        })
        .catch(error => {
            console.error('There was a problem with the fetch operation:', error);
        });
}

function wait(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

async function loadPageContent() {
    windowLoading();
    createHeader();
    createHome();
    createPiano();
    await wait(240);   // Wait .24 seconds for content to load
    windowLoaded();
}

window.addEventListener('load', () => {
    loadPageContent();
});