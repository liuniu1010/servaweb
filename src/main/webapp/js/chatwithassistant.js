// Chat with Assistant page-specific logic

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

// Override functions specific to this page
function enableSendWithInput(input) {
    isRecording = false;
    document.getElementById("ButtonSendAudio").disabled = false;
    document.getElementById("ButtonSend").disabled = false;
    document.getElementById("UserInput").disabled = false;
    document.getElementById("UserInput").value = input;
    document.getElementById("UserInput").focus();
}

function enableSend() {
    document.getElementById("ButtonSendAudio").disabled = false;
    document.getElementById("ButtonSend").disabled = false;
    document.getElementById("UserInput").disabled = false;
    document.getElementById("UserInput").value = "";
    document.getElementById("UserInput").focus();
}

function disableSend() {
    document.getElementById("ButtonSendAudio").disabled = true;
    document.getElementById("ButtonSend").disabled = true;
    document.getElementById("UserInput").disabled = true;
}

function handleKeyPressForInput(event) {
    if (event.keyCode === 13 && !event.shiftKey) {
        event.preventDefault();
        sendEcho();
    }
}

function refresh() {
    var refreshUrl = "api/aichatwithassistant/refresh";
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
    var newChatUrl = "api/aichatwithassistant/newchat";
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
    var echoUrl = "api/aichatwithassistant/echo";
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
                sendChat();
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

function sendChat() {
    var sendUrl = "api/aichatwithassistant/send";
    var userInput = document.getElementById("UserInput").value;
    if(isStringEmptyOrWhitespace(userInput)) {
        enableSend();
        return false;
    }
    $.ajax({
        url: sendUrl,
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
            document.getElementById("RichChatRecord").innerHTML = error;
            document.getElementById("UserInput").value = "";
            enableSend();
        },
        complete: function() {
            document.getElementById("UserInput").value = "";
        }
    });
    document.getElementById("UserInput").value = "Please wait...";
}

function handleKeyPressForInput(event) {
    if (event.keyCode === 13 && !event.shiftKey) {
        event.preventDefault();
        sendEcho();
    }
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

document.getElementById("ButtonSendAudio").addEventListener("click", function() {
    sendAudio();
});

document.getElementById("ButtonSend").addEventListener("click", function() {
    sendEcho();
});

document.getElementById("UserInput").addEventListener("keydown", function(event) {
    handleKeyPressForInput(event);
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
