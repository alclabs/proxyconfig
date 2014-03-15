$(function() {

    // load initial config data
    loadConfig();

    function loadConfig() {
        $.get("config").done(function(data, textStatus, jqXHR ) {
            updateScreen(data);
        })
    }

    function updateScreen(data) {
        $("#auto").prop("checked", data.auto);
        $("#useproxy").prop("checked", data.useproxy);

        showOrHideHostPort(data.useproxy);

        var host = data.host || "";
        if (host.length == 0) {
            host = "No Proxy"
        }
        $("#host").val(host);

        var port = data.port || -1;
        if (port == -1) {
            port = "";
        }
        $("#port").val(port);

        if (data.auto) {
            $("#host,#port,#useproxy").prop("disabled", "disabled");
        } else {
            $("#host,#port,#useproxy").removeProp("disabled");
        }

        $("#authcheck").prop("checked", data.authreq);
        $("#username").val(data.username);
        $("#password").val(data.password);
        showUserPass();

        $("#testurl").val(data.testurl || "");

        var $result = $("#result");
        $result.css("color", "#000");
        $result.text("Testing ...");
        $.get("proxytest").done(function(data) {
            if (data.good) {
                $result.text("Test through proxy WORKED");
                $result.css("color", "#0A0");
            } else {
                var msg = data.errormsg || "Error";
                $result.text(msg);
                $result.css("color", "#B00");
            }
        });
    }

    $("#useproxy").on('click', function() {
        showOrHideHostPort($(this).prop("checked"));
    });

    function showOrHideHostPort(show) {
        if (show) {
            $("#hostport").show();
        } else {
            $("#hostport").hide();
        }
    }

    /* Post saved changes to server */
    $("#save").click(function( event ) {
        $("#result").text(""); // clear results before sending new data

        var data = {
            'auto': $("#auto").prop("checked"),
            'useproxy' : $("#useproxy").prop("checked"),
            'host': $("#host").val(),
            'port': $("#port").val(),
            'testurl' : $("#testurl").val(),
            'authreq' : $("#authcheck").prop("checked"),
            'username': $("#username").val(),
            'password': $("#password").val()
        };

        $.post("config", data).done(function(data, textStatus, jqXHR) {
            updateScreen(data);
        });
        event.preventDefault();
    });

    $("#auto").on("click", function() {
        $("#host,#port,#useproxy").prop("disabled", $(this).prop("checked"));
    });

    $("#authcheck").on("click", function() { showUserPass(); });

    function showUserPass() {
        if ($("#authcheck").prop("checked")) {
            $("#userpass").show();
        } else {
            $("#userpass").hide();
        }
    }

});