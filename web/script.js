import * as API from "./ApiCaller.js"

const hamburger_element = document.querySelector(".hamburger");
let isopened = false;
hamburger_element.addEventListener("click", ()=>{
    if(isopened){
        document.querySelector(".menu").style.transform = "translateX(88%)";
        hamburger_element.style.transform = "translateY(0px) rotate(0deg)";
        isopened = false;
    }else{
        document.querySelector(".menu").style.transform = "translateX(0%)";
        hamburger_element.style.transform = "translateY(1px) rotate(180deg)";
        isopened = true;
    }
});

const option_buttons = [...document.getElementsByClassName("option-btn")];

option_buttons.forEach((btn, index) => {
    btn.addEventListener("click", () => {
        showpanel(index);
    });
});

function showpanel(index) {
    console.log("Button clicked at index:", index);
    document.querySelectorAll(".panel").forEach(panel =>{
        panel.classList.remove("active");
    })
    switch (index) {
        case 0:
          document.getElementById("send-panel").classList.add("active");
          break;
        case 1:
          document.getElementById("Recive-panel").classList.add("active");


          break;
        default:
          document.getElementById("setting-panel").classList.add("active");
      }
}

API.getinfo();