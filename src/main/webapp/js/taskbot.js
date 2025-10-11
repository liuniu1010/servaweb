// Task Bot page-specific logic

window.onload = function() {
    streamrefresh();
}

// Override UI state functions
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
    document.getElementById("UserInput").value = "Please wait...";
}

function refresh() {
}

function streamrefresh() {
    disableSend();
    var streamrefreshUrl = "api/aitaskbot/streamrefresh";
    var userInput = "";
    $.ajax({
        url: streamrefreshUrl,
        type: "POST",
        data: JSON.stringify({loginSession: getLoginSession(), userInput: userInput}),
        cache: false,
        contentType: "application/json; charset=utf-8",
        xhrFields: {
            onprogress: function(e) {
                var newText = e.target.responseText;
                handleOnProcessText(newText);

                // Delay the scrolling code by 100ms to ensure that the content is fully loaded
                setTimeout(function() {
                    chatRecordEditor = document.getElementById("RichChatRecord");
                    chatRecordEditor.scrollTop = chatRecordEditor.scrollHeight;
                }, 100);
            }
        },
        success: function (data) {
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
    logoutMenu.style.display = "none";
}

function newChat() {
    var newChatUrl = "api/aitaskbot/newchat";
    var userInput = document.getElementById("UserInput").value;
    $.ajax({
        url: newChatUrl,
        type: "POST",
        data: JSON.stringify({loginSession: getLoginSession(), userInput: userInput}),
        cache: false,
        contentType: "application/json; charset=utf-8",
        success: function (data) {
            showEmbeddedContent("");
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

var xhr;
function streamsend() {
    var userInput = document.getElementById("UserInput").value;
    var streamsendUrl = "api/aitaskbot/streamsend";
    disableSend();
    if(isStringEmptyOrWhitespace(userInput)) {
        enableSend();
        return false;
    }
    xhr = $.ajax({
        url: streamsendUrl,
        type: "POST",
        data: JSON.stringify({loginSession: getLoginSession(), userInput: userInput}),
        cache: false,
        contentType: "application/json; charset=utf-8",
        xhrFields: {
            onprogress: function(e) {
                var newText = e.target.responseText;
                handleOnProcessText(newText);

                // Delay the scrolling code by 100ms to ensure that the content is fully loaded
                setTimeout(function() {
                    chatRecordEditor = document.getElementById("RichChatRecord");
                    chatRecordEditor.scrollTop = chatRecordEditor.scrollHeight;
                }, 100);
            }
        },
        success: function (data) {
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

// Override auth functions for admin API
function sendPassword() {
    var username = document.getElementById("username").value;
    if (!validateEmail(username)) {
        alert("Please enter a valid email address.");
        return;
    }
    disableSendPasswordButton();

    var sendpasswordUrl = "api/aiadminlogin/sendpassword";
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
    var loginUrl = "api/aiadminlogin/login";
    $.ajax({
        url: loginUrl,
        type: "POST",
        data: JSON.stringify({loginSession: username, userInput: password}),
        cache: false,
        contentType: "application/json; charset=utf-8",
        success: function (data) {
            saveLoginSession(data.message);
            document.getElementById("loginDialog").style.display = "None";
            streamrefresh();
        },
        error: function(xhr, status, error) {
            alertError(xhr, status, error);
        },
        complete: function() {
        }
    });
}

function logout() {
    var logoutUrl = "api/aiadminlogin/logout";
    $.ajax({
        url: logoutUrl,
        type: "POST",
        data: JSON.stringify({loginSession: getLoginSession(), userInput: ""}),
        cache: false,
        contentType: "application/json; charset=utf-8",
        success: function (data) {
            if (xhr && xhr.readyState !== 4) { // Check if the request is still in progress
                xhr.abort(); // Abort the ongoing AJAX request
            }
            saveLoginSession("");
            streamrefresh();
        },
        error: function(xhr, status, error) {
            saveLoginSession("");
            streamrefresh();
        },
        complete: function() {
        }
    });
}

function handleOnProcessText(newText) {
    if(isStringEmptyOrWhitespace(newText)) {
        enableSend();
        return;
    }

    var END_MARK = "-----end-----";
    showEmbeddedContent(newText);
    if(newText.endsWith(END_MARK)) {
        enableSend();
    }
}

function showEmbeddedContent(content) {
    document.getElementById("RichChatRecord").innerHTML = content;
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

document.getElementById("ButtonNew").addEventListener("click", function() {
    newChat();
});

document.getElementById("ButtonSendAudio").addEventListener("click", function() {
    sendAudio();
});

document.getElementById("ButtonSend").addEventListener("click", function() {
    streamsend();
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
