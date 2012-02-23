*{
*  arg (required)
*  url (required)
*  pageParamPrefix (optional) Prefix ajouté au paramètre HTTP indiquant le numero de page.
*  #{pager pager, url:actionBridge.Application.index() /}
}*
%{
( _arg ) && ( _pager = _arg);
if(! _pager) {
throw new play.exceptions.TagInternalException("pager attribute cannot be empty for pagination tag");
}
if(! _url) {
throw new play.exceptions.TagInternalException("url attribute cannot be empty for pager tag");
}
}%
#{set separator: _url.toString().contains("?") ? "&" : "?" /}
#{set firstPage: _pager.page - (controllers.utils.Pager.MAX_PAGE - 1) / 2 /}
#{set lastPage: _pager.page + (controllers.utils.Pager.MAX_PAGE - 1) / 2 /}

#{if firstPage < 1 }
#{set firstDifference: firstPage < 1 ? 1 - firstPage: 0 /}
#{set firstPage: 1 /}
#{set lastPage: [lastPage + firstDifference, _pager.pageCount].min() /}
#{/if}

#{if lastPage > _pager.pageCount }
#{set lastDifference: lastPage > _pager.pageCount ? lastPage - _pager.pageCount: 0 /}
#{set lastPage: _pager.pageCount /}
#{set firstPage: [firstPage - lastDifference, 1].max() /}
#{/if}

<ul>
    #{if _pager.page > 1}<li><a href="${_url}${separator}${_pageParamPrefix}page=${_pager.page-1}" class="first-page">&laquo;</a></li>#{/if}
    #{else}<li><a class="first-page">&laquo;</a></li>#{/else}


    #{if firstPage > 1 }<li><a>...</a></li>#{/if}
    #{list items:firstPage..lastPage, as:'nb'}<li class="${_pager.page == nb ? 'current active' : ''}">
    <a href="${_url}${separator}${_pageParamPrefix}page=${nb}">${nb}</a></li>
    #{/list}
    #{if lastPage < _pager.pageCount }<li><a>...</a></li>#{/if}

    #{if _pager.page < _pager.pageCount}<li><a href="${_url}${separator}${_pageParamPrefix}page=${_pager.page+1}" class="last-page">&raquo;</a></li>#{/if}
    #{else}<li><a class="last-page">&raquo;</a></li>#{/else}
</ul>
