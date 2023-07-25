<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
<head>
    <title>API test</title>

    <%--JQuery--%>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>

    <!-- Latest compiled and minified CSS -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">

    <!-- Optional theme -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap-theme.min.css" integrity="sha384-rHyoN1iRsVXV4nD0JutlnGaslCJuC7uwjduW9SVrLvRYooPp2bWYgmgJQIXwl/Sp" crossorigin="anonymous">

    <!-- Latest compiled and minified JavaScript -->
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js" integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa" crossorigin="anonymous"></script>

    <style>
        .jsonblock .jsonblock {
            margin-left: 2em;
        }

        #outputPanel {
            padding-left: 2em;
        }

        .jsonblock .collapsed .caret {
            transform: rotate(-90deg);
            transition: 0.5s;
        }

        .blockheader span:hover, .blockheader strong:hover {
            text-decoration: underline;
            cursor: pointer;
        }

        .jsonblock {
            color: #5a5a5a;
        }
        .jsonblock .caret {
            margin-right: 3pt;
            margin-left: -1em;
        }

        .blockbody {
            border-left: 1pt dotted #d8d8d8;
        }

        .jsonblock span > span {
            color : #3f9bd4;
        }

        .panel-heading {
            height: 54px;
        }

    </style>
    <script>

        function createSimpleHtml(name, value) {
            return $('<span></span>').append('<strong>' + name + '</strong>: <span>' + value + '</span>');
        }

        function createArrayHtml(name, array) {
            let blockHeader = $('<div class="blockheader"></div>');
            blockHeader.append('<span class="caret"></span><strong>' + name + ' (' + array.length + ' items) [</strong>');
            let blockBody = $('<div class="blockbody"></div>');
            let blockFooter = $('<strong>]</strong>');

            for (let i=0; i < array.length; i++) {
                blockBody.append(createBlockHtml("Item " + i, array[i]));
            }

            blockHeader.click(function(){
                blockHeader.toggleClass("collapsed");
                blockBody.slideToggle("fast");
            });

            return [blockHeader, blockBody, blockFooter];
        }

        function createDictionaryHtml(name, dictionary) {
            let blockHeader = $('<div class="blockheader"></div>');
            blockHeader.html('<span class="caret"></span><strong>' + name + ' {' + '</strong>');
            let blockBody = $('<div class="blockbody"></div>');
            let blockFooter = $('<strong>}</strong>');

            for (let propName in dictionary) {
                if (dictionary.hasOwnProperty(propName)) {
                    blockBody.append(createBlockHtml(propName, dictionary[propName]));
                }
            }

            blockHeader.click(function(){
                blockHeader.toggleClass("collapsed");
                blockBody.slideToggle("fast");
            });


            return [blockHeader, blockBody, blockFooter];
        }


        function createBlockHtml(name, data) {

            let blockElement = $('<div class="jsonblock"></div>');
            if (!data) {
                blockElement.append(createSimpleHtml(name, data));
            } else {
                if (Array.isArray(data)) {
                    blockElement.append(createArrayHtml(name, data));
                } else {
                    if (typeof data === "object") {
                        blockElement.append(createDictionaryHtml(name, data));
                    } else {
                        blockElement.append(createSimpleHtml(name, data));

                    }
                }
            }

            return blockElement;
        }


        var textCtrl, postBtnCtrl, outputPanelCtrl, endpointCtrl, datatypeCtrl, copyBtnCtrl;

        function copyTextToClipboard(text) {
            var textArea = document.createElement("textarea");
            textArea.style.position = 'fixed';
            textArea.style.top = 0;
            textArea.style.left = 0;
            textArea.style.width = '2em';
            textArea.style.height = '2em';
            textArea.style.padding = 0;
            textArea.style.border = 'none';
            textArea.style.outline = 'none';
            textArea.style.boxShadow = 'none';
            textArea.style.background = 'transparent';

            textArea.value = text;

            document.body.appendChild(textArea);

            textArea.select();

            try {
                var successful = document.execCommand('copy');
                var msg = successful ? 'successful' : 'unsuccessful';
                console.log('Copying text command was ' + msg);
            } catch (err) {
                console.log('Oops, unable to copy');
            }

            document.body.removeChild(textArea);
        }

        $(function() {
            textCtrl = $('#text');
            postBtnCtrl = $('#postBtn');
            outputPanelCtrl = $('#outputPanel');
            endpointCtrl = $('#endpoint');

            datatypeCtrl = $('#datatype');
            copyBtnCtrl = $('#copy');
            let resultsIndicator = $('#resultsIndicator');

            let json_panel = $('#json_panel');

            let jsonText = "";

            postBtnCtrl.click(function() {
                var endpoint = endpointCtrl.val();
                var datatype = datatypeCtrl.val();
                var text = textCtrl.val();
                if (endpoint && text) {
                    textCtrl.attr('disabled', true);
                    postBtnCtrl.prop('disabled', true);
                    endpointCtrl.prop('disabled', true);

                    outputPanelCtrl.html();

                    resultsIndicator.removeClass('glyphicon-remove-sign glyphicon-refresh glyphicon-ok-sign');
                    resultsIndicator.addClass('glyphicon-refresh');
                    json_panel.removeClass('panel-default panel-success panel-danger');
                    json_panel.addClass('panel-info');

                    var jqxhr =
                        $.ajax({
                            url: endpoint,
                            dataType: 'json',
                            type: 'post',
                            data: {
                                inputText: text,
                                datatype: datatype
                            }
                        })
                            .done(function(data) {
                                resultsIndicator.removeClass('glyphicon-remove-sign glyphicon-refresh glyphicon-ok-sign');
                                resultsIndicator.addClass('glyphicon-ok-sign');
                                json_panel.removeClass('panel-default panel-info panel-danger');
                                json_panel.addClass('panel-success');

                                jsonText = JSON.stringify(data, null, '\t');
                                let html = createBlockHtml("JSON Data", data);
                                outputPanelCtrl.html(html);

                            })
                            .fail(function()     {
                                resultsIndicator.removeClass('glyphicon-remove-sign glyphicon-refresh glyphicon-ok-sign');
                                resultsIndicator.addClass('glyphicon-remove-sign');
                                json_panel.removeClass('panel-default panel-success panel-info');
                                json_panel.addClass('panel-danger');
                            })
                            .always(function () {

                                textCtrl.attr('disabled', false);
                                postBtnCtrl.prop('disabled', false);
                                endpointCtrl.prop('disabled', false);
                            })
                    ;
                }

            });

            copyBtnCtrl.click(function() {
                copyTextToClipboard(jsonText);
            });

            $('form').submit(function(e){
                e.originalEvent.preventDefault();
            })

        });

    </script>

    <style>
        @keyframes rotate360 {
            to { transform: rotate(360deg); }
        }

        .glyphicon-refresh {
            animation: 2s rotate360 infinite linear;;
        }

        optgroup[label="Cars"]
        {
            color: red;
        }
    </style>
</head>

<body>

<div class="container-fluid">
    <div class="row">
        <div class="col-md-12">
            <h2 style="border-bottom: 1pt solid #dddddd">API test</h2>
        </div>
    </div>
    <div class="row">
        <div class="col-md-6">

            <form>
                <div class="form-group">
                    <label for="endpoint">Endpoint</label>
                    <div class="input-group">
                        <select class="form-control" id="endpoint">
                            <c:forEach items="${endpoints}" var="endpoint">
                                <optgroup label="${endpoint.key}">
                                    <c:forEach items="${endpoint.value}" var="url">
                                        <option value="${baseUrl}${url}">${url}</option>
                                    </c:forEach>
                                </optgroup>
                            </c:forEach>
                        </select>
                        <span class="input-group-btn">
                        <button class="btn btn-primary" id="postBtn">POST</button>
                    </span>

                    </div>

                </div>
                <div class="form-group">
                    <label for="text">Text to analyze</label>
                    <textarea id="text" class="form-control" rows="20"></textarea>
                </div>

                <div class="form-group hidden">
                    <label for="endpoint">Data type</label>
                    <select class="form-control" id="datatype">
                        <option value="plain_text" selected>Plain text</option>
                        <%--<option value="ccda">XML text (CCDA)</option>--%>

                    </select>
                </div>


            </form>

        </div>
        <div class="col-md-6">
            <div id="json_panel" class="panel panel-default">
                <div class="panel-heading">
                    <div style="margin: 7px 0; display: inline-block;" class="">
                        <span id="resultsIndicator" class="glyphicon" aria-hidden="true"></span>
                        Results
                    </div>
                    <button id="copy" type="button" class="btn btn-success pull-right">Copy to clipboard</button>
                </div>
                <div class="panel-body" id="outputPanel" style="max-height: 100%;overflow-y: scroll">

                </div>
            </div>
        </div>
    </div>

</div>


</body>
</html>