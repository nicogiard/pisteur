#{set 'moreScripts'}
    #{get 'moreScripts' /}
    <script type="text/javascript" src="@{'/public/javascripts/bootstrap.js'}"></script>
    <script type="text/javascript" src="http://bp.yahooapis.com/2.4.21/browserplus-min.js"></script>
    <script type="text/javascript" src="@{'/public/javascripts/plupload/plupload.js'}"></script>
    <script type="text/javascript" src="@{'/public/javascripts/plupload/plupload.gears.js'}"></script>
    <script type="text/javascript" src="@{'/public/javascripts/plupload/plupload.silverlight.js'}"></script>
    <script type="text/javascript" src="@{'/public/javascripts/plupload/plupload.flash.js'}"></script>
    <script type="text/javascript" src="@{'/public/javascripts/plupload/plupload.browserplus.js'}"></script>
    <script type="text/javascript" src="@{'/public/javascripts/plupload/plupload.html4.js'}"></script>
    <script type="text/javascript" src="@{'/public/javascripts/plupload/plupload.html5.js'}"></script>
    <script type="text/javascript">
        var uploader = {
            plupload : null,

            init : function(){
                var self = this;
                self.plupload = new plupload.Uploader({
                    "runtimes" : "html5,flash,gears,silverlight,browserplus",
                    "browse_button" : "addMulitpleTorrents",
                    "max_file_size" : "10mb",
                    "url" : "${_uploadUrl}",
                    "flash_swf_url" : '@{'/public/javascripts/plupload/plupload.flash.swf'}',
                    "silverlight_xap_url" : '@{'/public/javascripts/plupload/plupload.silverlight.xap'}',
                    "filters" : [
                        {"title" : "Torrent files", "extensions" : "torrent"}
                    ]
                });

                self.plupload.bind('FilesAdded', function(up, files) {
                    $(files).each(function(i, file){
                        var truncatedFileName = file.name;
                        if(truncatedFileName.length > 30){
                            truncatedFileName = truncatedFileName.substring(0, 30) + "...";
                        }
                        var fileinfo = $('<li></li>').attr("id",file.id).text(truncatedFileName + " " + plupload.formatSize(file.size)).css("overflow","hidden");
                        fileinfo.append(self.progressbar("progress-"+file.id));
                        $('#files-list').append(fileinfo);
                    });
                    $('#modal-upload-manager').modal({keyboard: false});
                    $('#modal-upload-manager').modal('show');
                });

                self.plupload.bind('UploadProgress', function(up, file) {
                    $('.bar','#progress-'+file.id).css("width",file.percent + "%");
                    $('.bar','#main-progressbar').css("width",up.total.percent + "%");
                    $('#close-upload-manager').attr("disabled","disabled");
                    $('#upload-files').attr("disabled","disabled");
                });

                self.plupload.bind('FileUploaded', function(up, file) {
                    $('#progress-'+file.id).removeClass("active");
                });

                self.plupload.bind('UploadComplete',function(up,files){
                    location.href = "${_returnUrl}";
                    return false;
                });

                $('#upload-files').click(function() {
                    if($(this).attr("disabled")){
                        return;
                    }
                    self.plupload.start();
                    return false;
                });

                self.plupload.init();
            },

            progressbar : function(id){
                var progress = $('<div></div>').attr("id",id)
                    .addClass("progress progress-info progress-striped active")
                    .css({
                        "width":"200px",
                        "float":"right"
                    });
                var bar = $('<div></div>').addClass("bar").css({"width":"0%"});
                progress.html(bar);
                return progress;
            },

            refresh : function(){
                var self = this;
                $('.bar','#main-progressbar').css("width","0%");
                $('#files-list').empty();
                $('#main-progressbar').addClass("active");
                self.plupload.splice(0,self.plupload.files.length);
                $('#close-upload-manager').removeAttr("disabled");
                $('#upload-files').removeAttr("disabled");
            }
        }

        $(document).ready(function(){
            uploader.init();
            $('#close-upload-manager').click(function(){
                if($(this).attr("disabled")){
                    return;
                }
                $('#modal-upload-manager').modal('hide');
            });
            $('#modal-upload-manager').on('hide', function () {
                uploader.refresh();
            });
        });

    </script>
#{/set}

<div class="modal hide" id="modal-upload-manager" style="width:600px;margin-left:-300px;">
    <div class="modal-header">
        <h3>&{'cockpit.title.uploadManager'}</h3>
    </div>
    <div class="modal-body">
        <ul id="files-list"  style="height:190px;overflow:auto;padding-right:20px;"></ul>
        <div id="main-progressbar" class="progress progress-danger progress-striped active">
            <div class="bar" style="width: 0%;"></div>
        </div>
        <div class="form-actions">
            <a id="upload-files" href="javascript:;" class="btn btn-primary" title="Envoyer"><i class="icon-upload icon-white"></i>&nbsp;Envoyer</a>
            <a id="close-upload-manager" class="btn" title="Annuler"><i class="icon-remove"></i>&nbsp;Annuler</a>
        </div>
    </div>
</div>