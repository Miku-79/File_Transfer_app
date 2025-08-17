export function status(){
    fetch('http://localhost:8080/status')
    .then(res => res.text())
    .then(data => {
        console.log("Backend says:",data);
    })
}

export function ReadyRecive(){

    fetch("http://localhost:8080/API/Recive")
}

export function ReadySend(){

    fetch("http://localhost:8080/API/Send")
}

export function getinfo(){
    fetch("http://localhost:8080/API/Getinfo")
  .then(response => response.json())        
  .then(cfg => {                            
    console.log(cfg.device_name);
    console.log(cfg.port);
    console.log(cfg.Machine_ip);
  })
}