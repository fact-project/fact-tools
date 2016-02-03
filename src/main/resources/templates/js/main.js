$.getJSON('http://hipsterjesus.com/api/?paras=2&type=hipster-centric', function(data) {
    $('#hipstercontent').html( data.text );
});