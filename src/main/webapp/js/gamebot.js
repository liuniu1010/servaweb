// Game Bot page-specific logic

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

// Override UI state functions with undo button and image input
function enableSendWithInput(input) {
    isRecording = false;
    document.getElementById("undo").disabled = false;
    document.getElementById("ButtonSendAudio").disabled = false;
    document.getElementById("ButtonSend").disabled = false;
    document.getElementById("UserInput").disabled = false;
    document.getElementById("UserInput").value = input;
    document.getElementById("UserInput").focus();
}

function enableSend() {
    isRecording = false;
    document.getElementById("undo").disabled = false;
    document.getElementById("ButtonSendAudio").disabled = false;
    document.getElementById("ButtonSend").disabled = false;
    document.getElementById("UserInput").disabled = false;
    document.getElementById('imgInput').value = "";
    document.getElementById("UserInput").value = "";
    document.getElementById("UserInput").focus();
}

function disableSend() {
    document.getElementById("undo").disabled = true;
    document.getElementById("ButtonSendAudio").disabled = true;
    document.getElementById("ButtonSend").disabled = true;
    document.getElementById("UserInput").disabled = true;
    document.getElementById("UserInput").value = "Please wait...";
}

function showEmbeddedContent(content) {
    const iframe = document.createElement('iframe');
    iframe.style.width = "100%";
    iframe.style.height = "100%";
    iframe.style.border = "none";
    iframe.srcdoc = content;
    iframe.setAttribute("sandbox", "allow-scripts allow-same-origin");
    const container = document.getElementById("RichChatRecord");
    container.innerHTML = "";
    container.appendChild(iframe);
}

function newChat() {
    var newChatUrl = "api/aiutilitybot/newchat";
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

function undo() {
    var undoUrl = "api/aiutilitybot/undo";
    var userInput = "";
    disableSend();
    $.ajax({
        url: undoUrl,
        type: "POST",
        data: JSON.stringify({loginSession: getLoginSession(), userInput: userInput}),
        cache: false,
        contentType: "application/json; charset=utf-8",
        xhrFields: {
            onprogress: function(e) {
                var newText = e.target.responseText;
                handleOnProcessText(newText);
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

function handleOnProcessText(newText) {
    if(isStringEmptyOrWhitespace(newText)) {
        enableSend();
        return;
    }

    var ENDOFINPUT = "*****ENDOFINPUT*****";
    var ENDOFCODE = "*****ENDOFCODE*****";

    var separators = [ENDOFINPUT, ENDOFCODE];
    newText = newText.trim();
    if(newText.endsWith(ENDOFINPUT)
       || newText.endsWith(ENDOFCODE)) {
        var splitText = splitString(newText, separators);
        var secondLast = splitText[splitText.length - 2];
        showEmbeddedContent(secondLast);

        if(newText.endsWith(ENDOFCODE)) {
            enableSend();
        }
    }
}

var xhr;
function streamsend(base64String) {
    var streamsendUrl = "api/aiutilitybot/streamsend";
    var userInput = document.getElementById("UserInput").value;
    var theFunction = "utilitybot";
    disableSend();
    if(isStringEmptyOrWhitespace(userInput) && isStringEmptyOrWhitespace(base64String)) {
        enableSend();
        return false;
    }
    xhr = $.ajax({
        url: streamsendUrl,
        type: "POST",
        data: JSON.stringify({loginSession: getLoginSession(), userInput: userInput, fileAsBase64: base64String, theFunction: theFunction}),
        cache: false,
        contentType: "application/json; charset=utf-8",
        xhrFields: {
            onprogress: function(e) {
                var newText = e.target.responseText;
                handleOnProcessText(newText);
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
    var streamrefreshUrl = "api/aiutilitybot/streamrefresh";
    var userInput = "";
    disableSend();
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
    showCreditCount();
    showPaymentLink();
}

// Override login/logout to use streamrefresh
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

function downloadIframeContent() {
    const iframe = document.querySelector("#RichChatRecord iframe");
    if (!iframe) {
        return;
    }

    const htmlContent = iframe.srcdoc;
    if( isStringEmptyOrWhitespace(htmlContent)) {
        return;
    }
    const blob = new Blob([htmlContent], { type: "text/html" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "game.html";
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
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
    var fileInputEdit = document.getElementById('fileInputEdit');
    var file = fileInputEdit.files[0];

    if (file) {
        getFileBase64(file)
            .then(base64String => {
                streamsend(base64String);
            })
        .catch(error => {
            console.error('Error converting file to base64:', error);
            enableSend();
        });
    }
}

function splitString(input, separators) {
    // Escape regex-special characters in each separator
    const escapedSeparators = separators.map(sep => sep.replace(/[-\/\\^$*+?.()|[\]{}]/g, '\\$&'));

    // Join separators using regex alternation (|)
    const regex = new RegExp(escapedSeparators.join('|'), 'g');

    return input.split(regex);
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
    const userConfirmed = confirm("Current content will be cleared, do you really want to start a new game?");
    if(userConfirmed) {
        newChat();
    }
});

document.getElementById("ButtonEdit").addEventListener("click", function() {
    const fileInput = document.getElementById('fileInputEdit');
    fileInput.value = "";
    fileInput.click();
});

document.getElementById('fileInputEdit').addEventListener('change', function () {
    const userConfirmed = confirm("Current content will be cleared, do you really want to open this file?");
    if(!userConfirmed) {
        return;
    }

    var file = this.files[0];
    if (file) {
        var fileType = file.name.split('.').pop().toLowerCase();
        if (fileType !== 'htm' && fileType !== 'html') {
            alert('Only html files are allowed.');
            this.value = '';
        }
        else if (file.size > 10 * 1024 * 1024) {
            alert("The selected html file exceeds the 10MB limit. Please choose a smaller file.");
            this.value = '';
        }
        else {
            encodeInputFileAndSend();
        }
    }
});

document.getElementById('imgInput').addEventListener('change', function() {
    var file = this.files[0];
    if (file) {
        var fileType = file.name.split('.').pop().toLowerCase();
        if (fileType !== 'png' && fileType !== 'webp' && fileType !== 'jpg') {
            alert('Only image files are allowed.');
            this.value = '';
        }
        else if (file.size > 10 * 1024 * 1024) {
            alert("The selected image file exceeds the 10MB limit. Please choose a smaller file.");
            this.value = '';
        }
    }
});

document.getElementById("undo").addEventListener("click", function() {
    undo();
});

document.getElementById("downloadBtn").addEventListener("click", function() {
    downloadIframeContent();
});

document.getElementById("ButtonSendAudio").addEventListener("click", function() {
    sendAudio();
});

document.getElementById("ButtonSend").addEventListener("click", function() {
    streamsend('');
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
