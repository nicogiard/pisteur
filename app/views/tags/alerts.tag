#{set 'moreScripts'}
    #{get 'moreScripts'/}
    <script type="text/javascript">
        $(document).ready(function(){
            $('.alert-success').delay(2000).slideUp();
        });
    </script>
#{/set}

#{if  flash.get((_prefix!=null?_prefix:'') + 'success')}
<div class="alert alert-success">
    <a class="close" data-dismiss="alert" title="Fermer">×</a>
    &{flash.get((_prefix!=null?_prefix:'') + 'success')}
</div>
#{/if}
#{if  flash.get((_prefix!=null?_prefix:'') + 'warning')}
<div class="alert">
    <a class="close" data-dismiss="alert" title="Fermer">×</a>
    &{flash.get((_prefix!=null?_prefix:'') + 'warning')}
</div>
#{/if}
#{if  flash.get((_prefix!=null?_prefix:'') + 'error')}
<div class="alert alert-error">
    <a class="close" data-dismiss="alert" title="Fermer">×</a>
    &{flash.get((_prefix!=null?_prefix:'') + 'error')}
</div>
#{/if}