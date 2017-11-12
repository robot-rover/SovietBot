console.log('This is be the main JS file.');
function toggleAccordion(element){
    /* Toggle between adding and removing the "active" class,
    to highlight the button that controls the panel */
    element.classList.toggle("active");

    /* Toggle between hiding and showing the active panel */
    var panel = element.nextElementSibling;
    if (panel.style.display === "block") {
        panel.style.display = "none";
    } else {
        panel.style.display = "block";
    }
}