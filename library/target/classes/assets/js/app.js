$(document).ready(function(){
    var allData = $('td[title="status"]');
    for( i=0; i<allData.length; i++ ){
            var bookStatus = allData[i].innerHTML;
            var numId1 = allData[i].id.slice("6");
            var numId2 = "#"+numId1;
            if(bookStatus == "lost")
                    {
                            $(numId2).attr("disabled","disabled");
                    }
            else{
                    $(numId2).removeAttr("disabled");
            }
    }
});

$(":button").click(function() {
        var bookIsbn = this.id;
        var URI = "/library/v1/books/"+bookIsbn+"?status=lost";
        var disableButton = "#"+bookIsbn;
        var bookStatusnew = "#status"+bookIsbn;
        alert('About to report lost on bookIsbn ' + bookIsbn);
        
        $.ajax({
                  url: URI,
                  type: 'PUT',
                  success: function(data) {
                          $(disableButton).attr("disabled","disabled");
                            $(bookStatusnew).text("lost");
                  }
                });
});