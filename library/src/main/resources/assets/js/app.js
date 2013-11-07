function notifyProcurement(lostBookIsbn) {
    var isbn = lostBookIsbn;
    
    $.ajax({
        url: '/library/v1/books/' + isbn + '/?status=lost',
       rawBody: "json",
        type: 'PUT',
        success: function(data) {
                        alert('The book with isbn :: ' + isbn+ ' is lost.');
                        window.location.reload();
                }
        });
};
