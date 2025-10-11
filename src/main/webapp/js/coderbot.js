// Coder Bot page-specific logic

window.onload = function() {
    const urlParams = new URLSearchParams(window.location.search);
    const loginSession = urlParams.get("loginSession");
    if(loginSession) {
        saveLoginSession(loginSession);
        window.history.replaceState({}, document.title, window.location.pathname);
        location.reload();
    }
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

function showCreditCount() {
    var getCreditCountUrl = "api/aiclientcredit/getcreditcount";
    $.ajax({
        url: getCreditCountUrl,
        type: "POST",
        data: JSON.stringify({loginSession: getLoginSession()}),
        cache: false,
        contentType: "application/json; charset=utf-8",
        success: function (data) {
            const creditLabel = document.getElementById("creditCount");
            if (creditLabel) {
                creditLabel.innerText = "Credits Left: " + data.message;
            }
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

function showPaymentLink() {
    var getPaymentLinkUrl = "api/aiclientcredit/getpaymentlink";
    $.ajax({
        url: getPaymentLinkUrl,
        type: "POST",
        data: JSON.stringify({loginSession: getLoginSession()}),
        cache: false,
        contentType: "application/json; charset=utf-8",
        success: function (data) {
            const a = document.getElementById("purchaseLink");
            a.href = data.message;
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

function streamrefresh() {
    disableSend();
    var streamrefreshUrl = "api/aicoderbot/streamrefresh";
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
        },
        complete: function() {
            document.getElementById("UserInput").value = "";
        }
    });
    logoutMenu.style.display = "none";
    showCreditCount();
    showPaymentLink();
}

function newChat() {
    var newChatUrl = "api/aicoderbot/newchat";
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
    var streamsendUrl = "api/aicoderbot/streamsend";
    var userInput = document.getElementById("UserInput").value;
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

function downloadFile(url, fileName) {
    var link = document.createElement('a');
    link.href = url;
    link.download = fileName;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

// Override login to use streamrefresh
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
            streamrefresh();
        },
        error: function(xhr, status, error) {
            alertError(xhr, status, error);
        },
        complete: function() {
        }
    });
}

// Override logout to use streamrefresh and abort xhr
function logout() {
    var logoutUrl = "api/aiclientlogin/logout";
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
    if(newText.trim().endsWith(END_MARK)) {
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

document.getElementById("googleLoginBtn").addEventListener("click", function() {
    loginWithGoogle();
});

document.getElementById("microsoftLoginBtn").addEventListener("click", function() {
    loginWithMicrosoft();
});
