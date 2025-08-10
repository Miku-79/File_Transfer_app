function status(){
    fetch('http://localhost:8080/status')
    .then(res => res.text())
    .then(data => {
        console.log("Backend says:",data);
    })
}

function ReadyRecive(){

    fetch("http://localhost:8080/API/Recive")
}

