// Speech Split page-specific logic

window.onload = function() {
    const urlParams = new URLSearchParams(window.location.search);
    const loginSession = urlParams.get("loginSession");
    if(loginSession) {
        saveLoginSession(loginSession);
        window.history.replaceState({}, document.title, window.location.pathname);
        location.reload();
    }
    refresh();
}

// Override enableSend to reset file input
function enableSend() {
    document.getElementById("ButtonSend").disabled = false;
    document.getElementById("UserInput").disabled = false;
    document.getElementById("UserInput").focus();
    document.getElementById('fileInput').value = "";
}

function handleKeyPressForInput(event) {
    if (event.keyCode === 13 && !event.shiftKey) {
        event.preventDefault();
        sendEcho();
    }
}

function refresh() {
    var refreshUrl = "api/speechsplit/refresh";
    var userInput = document.getElementById("UserInput").value;
    $.ajax({
        url: refreshUrl,
        type: "POST",
        data: JSON.stringify({loginSession: getLoginSession(), userInput: userInput}),
        cache: false,
        contentType: "application/json; charset=utf-8",
        success: function (data) {
            document.getElementById('RichChatRecord').innerHTML = data.message;
            // Delay the scrolling code by 100ms to ensure that the content is fully loaded
            setTimeout(function() {
                chatRecordEditor = document.getElementById("RichChatRecord");
                chatRecordEditor.scrollTop = chatRecordEditor.scrollHeight;
                enableSend();
            }, 100);
        },
        error: function(xhr, status, error) {
            if (xhr.status === 401) {
                document.getElementById("loginDialog").style.display = "block";
            }
            else {
                alertError(xhr, status, error);
            }
            document.getElementById('RichChatRecord').innerHTML = error;
            enableSend();
        }
    });
    logoutMenu.style.display = "none";
}

function newChat() {
    var newChatUrl = "api/speechsplit/newchat";
    var userInput = document.getElementById("UserInput").value;
    $.ajax({
        url: newChatUrl,
        type: "POST",
        data: JSON.stringify({loginSession: getLoginSession(), userInput: userInput}),
        cache: false,
        contentType: "application/json; charset=utf-8",
        success: function (data) {
            document.getElementById('RichChatRecord').innerHTML = data.message;
            enableSend();
        },
        error: function(xhr, status, error) {
            if (xhr.status === 401) {
                document.getElementById("loginDialog").style.display = "block";
            }
            else {
                alertError(xhr, status, error);
            }
            document.getElementById('RichChatRecord').innerHTML = error;
            enableSend();
        }
    });
}

function sendEcho() {
    disableSend();
    var echoUrl = "api/speechsplit/echo";
    var userInput = document.getElementById("UserInput").value;
    if(isStringEmptyOrWhitespace(userInput)) {
        enableSend();
        return false;
    }
    $.ajax({
        url: echoUrl,
        type: "POST",
        data: JSON.stringify({loginSession: getLoginSession(), userInput: userInput}),
        cache: false,
        contentType: "application/json; charset=utf-8",
        success: function (data) {
            document.getElementById("RichChatRecord").innerHTML = data.message;
            // Delay the scrolling code by 100ms to ensure that the content is fully loaded
            setTimeout(function() {
                chatRecordEditor = document.getElementById("RichChatRecord");
                chatRecordEditor.scrollTop = chatRecordEditor.scrollHeight;
                encodeInputFileAndSend();
            }, 100);
        },
        error: function(xhr, status, error) {
            if (xhr.status === 401) {
                document.getElementById("loginDialog").style.display = "block";
            }
            else {
                alertError(xhr, status, error);
            }
            document.getElementById("RichChatRecord").innerHTML = error;
            document.getElementById("UserInput").value = "";
            enableSend();
        }
    });
}

async function getFileBase64(file) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => resolve(reader.result);
        reader.onerror = error => reject(error);
        reader.readAsDataURL(file);
    });
}

function encodeInputFileAndSend() {
    var fileInput = document.getElementById('fileInput');
    var file = fileInput.files[0];

    if (file) {
        getFileBase64(file)
            .then(base64String => {
                sendChat(base64String);
            })
        .catch(error => {
            console.error('Error converting file to base64:', error);
            enableSend();
        });
    }
    else {
        alert("Please select a valid mp3 file");
    }
}

function sendChat(base64String) {
    var sendUrl = "api/speechsplit/send";
    var userInput = document.getElementById("UserInput").value;
    if(isStringEmptyOrWhitespace(userInput)) {
        enableSend();
        return false;
    }
    $.ajax({
        url: sendUrl,
        type: "POST",
        data: JSON.stringify({loginSession: getLoginSession(), userInput: userInput, fileAsBase64: base64String}),
        cache: false,
        contentType: "application/json; charset=utf-8",
        success: function (data) {
            document.getElementById("RichChatRecord").innerHTML = data.message;
            // Delay the scrolling code by 100ms to ensure that the content is fully loaded
            setTimeout(function() {
                chatRecordEditor = document.getElementById("RichChatRecord");
                chatRecordEditor.scrollTop = chatRecordEditor.scrollHeight;
                enableSend();
            }, 100);
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
            document.getElementById("UserInput").value = "";
        }
    });
    document.getElementById("UserInput").value = "Please wait...";
}

// Event listeners
document.getElementById("ButtonAccount").addEventListener("click", function() {
    var logoutMenu = document.getElementById("logoutMenu");
    if (logoutMenu.style.display === "block") {
        logoutMenu.style.display = "none";
    } else {
        logoutMenu.style.display = "block";
    }
});

document.getElementById("ButtonNewChat").addEventListener("click", function() {
    newChat();
});

document.getElementById('fileInput').addEventListener('change', function() {
    var file = this.files[0];
    if (file) {
        var fileType = file.name.split('.').pop().toLowerCase();
        if (fileType !== 'mp3') {
            alert('Only MP3 files are allowed.');
            this.value = '';
        }
        else if (file.size > 10 * 1024 * 1024) {
            alert("The selected MP3 file exceeds the 10MB limit. Please choose a smaller file.");
            this.value = '';
        }
        else {
            document.getElementById("UserInput").value = file.name;
        }
    }
});

document.getElementById("ButtonSend").addEventListener("click", function() {
    sendEcho();
});

document.getElementById("logoutBtn").addEventListener("click", function() {
    logout();
});

document.getElementById("sendPasswordBtn").addEventListener("click", function() {
    sendPassword();
});

document.getElementById("loginBtn").addEventListener("click", function() {
    login();
});

document.getElementById("googleLoginBtn").addEventListener("click", function() {
    loginWithGoogle();
});

document.getElementById("microsoftLoginBtn").addEventListener("click", function() {
    loginWithMicrosoft();
});
