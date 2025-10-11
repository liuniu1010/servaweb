// Common utility functions used across multiple pages

// Cookie management functions
function getCookieValue(cookieName) {
    var cookies = document.cookie.split(';');
    for (var i = 0; i < cookies.length; i++) {
        var cookie = cookies[i].trim();
        if (cookie.indexOf(cookieName + '=') === 0) {
            return cookie.substring(cookieName.length + 1);
        }
    }
    return "";
}

function setCookieValue(cookieName, cookieValue) {
    document.cookie = cookieName + "=" + cookieValue + ";path=/";
}

function getLoginSession() {
    return getCookieValue("loginSession");
}

function saveLoginSession(loginSession) {
    setCookieValue("loginSession", loginSession);
}

// Utility functions
function isStringEmptyOrWhitespace(str) {
    if(str) {
        return (str.trim() == "");
    }
    else {
        return true;
    }
}

function downloadFile(url, fileName) {
    var link = document.createElement('a');
    link.href = url;
    link.download = fileName;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

// Email validation
function validateEmail(email) {
    var re = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    return re.test(String(email).toLowerCase());
}

// Error handling
function alertError(xhr, status, error) {
    var message = xhr.responseText;
    try {
        var jsonResponse = JSON.parse(xhr.responseText);
        message = jsonResponse.message || jsonResponse.error;
    }
    catch(e) {
    }
    alert(message + "\nFor more information, please contact niuliu285@gmail.com");
}

// UI state functions
function enableSend() {
    document.getElementById("ButtonSend").disabled = false;
    document.getElementById("UserInput").disabled = false;
    document.getElementById("UserInput").value = "";
    document.getElementById("UserInput").focus();
}

function disableSend() {
    document.getElementById("ButtonSend").disabled = true;
    document.getElementById("UserInput").disabled = true;
}

function enableSendWithInput(input) {
    document.getElementById("ButtonSend").disabled = false;
    document.getElementById("UserInput").disabled = false;
    document.getElementById("UserInput").value = input;
    document.getElementById("UserInput").focus();
}

function disableSendPasswordButton() {
    var sendPasswordBtn = document.getElementById("sendPasswordBtn");
    sendPasswordBtn.disabled = true;
    sendPasswordBtn.innerText = "Sending...";
}

function enableSendPasswordButton() {
    var sendPasswordBtn = document.getElementById("sendPasswordBtn");
    sendPasswordBtn.disabled = false;
    sendPasswordBtn.innerText = "Send Password";
}

// Authentication functions
function sendPassword() {
    var username = document.getElementById("username").value;
    if (!validateEmail(username)) {
        alert("Please enter a valid email address.");
        return;
    }
    disableSendPasswordButton();

    var sendpasswordUrl = "api/aiclientlogin/sendpassword";
    $.ajax({
        url: sendpasswordUrl,
        type: "POST",
        data: JSON.stringify({loginSession: username, userInput: ""}),
        cache: false,
        contentType: "application/json; charset=utf-8",
        success: function (data) {
            alert(data.message);
            enableSendPasswordButton();
        },
        error: function(xhr, status, error) {
            alertError(xhr, status, error);
            enableSendPasswordButton();
        },
        complete: function() {
            enableSendPasswordButton();
        }
    });
}

function login() {
    var username = document.getElementById("username").value;
    var password = document.getElementById("password").value;

    if(isStringEmptyOrWhitespace(username)) {
        return false;
    }
    var loginUrl = "api/aiclientlogin/login";
    $.ajax({
        url: loginUrl,
        type: "POST",
        data: JSON.stringify({loginSession: username, userInput: password}),
        cache: false,
        contentType: "application/json; charset=utf-8",
        success: function (data) {
            saveLoginSession(data.message);
            document.getElementById("loginDialog").style.display = "None";
            refresh();
        },
        error: function(xhr, status, error) {
            alertError(xhr, status, error);
        },
        complete: function() {
        }
    });
}

function logout() {
    var logoutUrl = "api/aiclientlogin/logout";
    $.ajax({
        url: logoutUrl,
        type: "POST",
        data: JSON.stringify({loginSession: getLoginSession(), userInput: ""}),
        cache: false,
        contentType: "application/json; charset=utf-8",
        success: function (data) {
            saveLoginSession("");
            refresh();
        },
        error: function(xhr, status, error) {
            saveLoginSession("");
            refresh();
        },
        complete: function() {
        }
    });
}

function loginWithGoogle() {
    var urlOfGoogleOAuthUrl = "api/aiclientlogin/getoauthgoogleurl";
    var originalPage = window.location.pathname;
    $.ajax({
        url: urlOfGoogleOAuthUrl,
        type: "POST",
        data: JSON.stringify({userInput: originalPage}),
        cache: false,
        contentType: "application/json; charset=utf-8",
        success: function (data) {
            window.location.href = data.message;
        },
        error: function(xhr, status, error) {
            if (xhr.status === 401) {
                document.getElementById("loginDialog").style.display = "block";
            }
            else {
                alertError(xhr, status, error);
            }
        }
    });
}

function loginWithMicrosoft() {
    var urlOfMicrosoftOAuthUrl = "api/aiclientlogin/getoauthmicrosofturl";
    var originalPage = window.location.pathname;
    $.ajax({
        url: urlOfMicrosoftOAuthUrl,
        type: "POST",
        data: JSON.stringify({userInput: originalPage}),
        cache: false,
        contentType: "application/json; charset=utf-8",
        success: function (data) {
            window.location.href = data.message;
        },
        error: function(xhr, status, error) {
            if (xhr.status === 401) {
                document.getElementById("loginDialog").style.display = "block";
            }
            else {
                alertError(xhr, status, error);
            }
        }
    });
}

// Audio recording functions
let isRecording = false;
let mediaRecorder;
let audioChunks = [];

const recordImageUrl = "images/micphonerunning.png";
const stopImageUrl = "images/micphone.png";

function sendAudio() {
    if (!isRecording) {
        isRecording = true;
        recordButtonImage.src = recordImageUrl;
        startRecording();
    } else {
        isRecording = false;
        recordButtonImage.src = stopImageUrl;
        stopRecording();
    }
}

async function startRecording() {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    mediaRecorder = new MediaRecorder(stream);
    mediaRecorder.ondataavailable = event => {
        audioChunks.push(event.data);
    };
    mediaRecorder.start();
}

function stopRecording() {
    mediaRecorder.stop();
    mediaRecorder.onstop = () => {
        const audioBlob = new Blob(audioChunks, { type: 'audio/mp3' });
        const reader = new FileReader();
        reader.readAsDataURL(audioBlob);
        reader.onloadend = () => {
            const rawBase64 = reader.result.split(',')[1];
            sendAudioToServer(rawBase64);
        };
        audioChunks = [];
    };
}

function sendAudioToServer(rawBase64) {
    var sendAudioUrl = "api/aispeechtotext/sendaudio";
    var userInput = document.getElementById("UserInput").value;
    var base64String = "data:audio/mpeg;base64," + rawBase64;
    disableSend();
    $.ajax({
        url: sendAudioUrl,
        type: "POST",
        data: JSON.stringify({loginSession: getLoginSession(), userInput: userInput, fileAsBase64: base64String}),
        cache: false,
        contentType: "application/json; charset=utf-8",
        success: function (data) {
            var input = data.message;
            enableSendWithInput(input);
        },
        error: function(xhr, status, error) {
            if (xhr.status === 401) {
                document.getElementById("loginDialog").style.display = "block";
            }
            else {
                alertError(xhr, status, error);
            }
            document.getElementById("UserInput").value = "";
            enableSend();
        },
        complete: function() {
        }
    });
}
