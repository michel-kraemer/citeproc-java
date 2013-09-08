$(document).ready(function() {
    //enable smooth scrolling
    //$('a').smoothScroll({ speed: 'auto' });
    
    var dontscroll = false;
    
    //enable smooth scrolling with back button support
    $(document).on('click', 'a[href*="#"]:not(.no-scroll)', function() {
      if (this.hash) {
        var tgt = this.hash.slice(1);
        $.bbq.pushState('#/' + tgt);
        if (document.getElementById(tgt)) {
          $.smoothScroll({scrollTarget: '#' + tgt});
        }
        return false;
      }
    });

    $(window).bind('hashchange', function(event) {
      var tgt = location.hash.replace(/^#\/?/, '');
      if (document.getElementById(tgt)) {
        if (dontscroll) {
          $.smoothScroll({scrollTarget: '#' + tgt, speed: 0});
        } else {
          $.smoothScroll({scrollTarget: '#' + tgt});
        }
        
        //after the first scroll (which happens after the page has been
        //loaded) enable scrolling
        dontscroll = false;
      }
    });

    if (window.location.hash) {
      dontscroll = true;
      $(window).trigger('hashchange');
    }
    
    //set last accessed data in samples to today
    var date = new Date();
    var year = date.getFullYear()        + 1;
    var month = date.getMonth() + 1            - 3;
    var day = date.getDate()                   - 4;
    var strday = "" + day;
    if (strday.length < 2) {
      strday = '0' + strday;
    }
    var monthnames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul',
      'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    var monthnamesmedium = ['Jan.', 'Feb.', 'Mar.', 'Apr.',
      'May', 'June', 'July', 'Aug.', 'Sept.', 'Oct.', 'Nov.', 'Dec.'];
    var monthnameslong = ['January', 'February', 'March', 'April',
      'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];
    var strmonth = monthnames[month - 1];
    var strmonthmedium = monthnamesmedium[month - 1];
    var strmonthlong = monthnameslong[month - 1];
    $('.sample code').each(function() {
      var h = $(this).html();
      //replace 'accessed(y, m, d)' in syntax highlighted code
      h = h.replace(/accessed<([^(]+)\(<([^0-9]+)[0-9]+<([^0-9]+)[0-9]+<([^0-9]+)[0-9]+/,
        'accessed<$1(<$2' + year + '<$3' + month + '<$4' + day);
      //replace '[Accessed: d-m-y]' in output
      h = h.replace(/\[Accessed\: [0-9]+-[a-zA-Z]+-[0-9]+\]/, '[Accessed: ' +
        strday + '-' + strmonth + '-' + year + ']');
      //replace 'Retrieved m d, y' in output
      h = h.replace(/Retrieved(\s+)[a-zA-Z]+ [0-9]+, [0-9]+/, 'Retrieved$1' +
        strmonthlong + ' ' + day + ', ' + year);
      //replace '[cited y m d]' in output
      h = h.replace(/\[cited [0-9]+ [a-zA-Z]+ [0-9]+\]/, '[cited ' +
        year + ' ' + strmonthlong + ' ' + day + ']');
      //replace 'Web. d m y' in output
      h = h.replace(/Web\. [0-9]+ [a-zA-Z\.]+ [0-9]+/, 'Web. ' +
        day + ' ' + strmonthmedium + ' ' + year);
      $(this).html(h);
    });
    
    $('.today-year').html(year);
    $('.today-month').html(strmonth);
    $('.today-monthmedium').html(strmonthmedium);
    $('.today-monthlong').html(strmonthlong);
    $('.today-daylong').html(strday);
    $('.today-day').html("" + day);
});
