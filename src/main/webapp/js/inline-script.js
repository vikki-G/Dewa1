
//---------mobile menu height---------//
// $(document).ready(
//     function () {
//         function setHeight() {
//             windowHeight = $(window).innerHeight();
//             $('.report-table-container').height(
//                 $(window).height() - 95 - $("header").height() - $(".head-content").height() - $(".form-body-container").height());
//         };
//         setHeight();

//         $(window).resize(function () {
//             setHeight();
//         });

//         $(window).scroll(function () {
//             setHeight();
//         });

//         //         $(".showhide-filter-btn, #txtSearch").click(function () {
//         //                 $(window).trigger('resize');                				
//         //});

//     });
//---------mobile menu height---------//


//---------mobile menu btn---------//
$(document).ready(function () {
    $(".menu-btn").click(function () {
        $(".admin-body-container").toggleClass("hide-submenu");
    });
});
//---------mobile menu btn---------//

//---------Page Loader---------//
$(document).ready(function () {
    $(document).ajaxStart(function () {
        $("#loader").css("display", "table");
    });
    $(document).ajaxComplete(function () {
        $("#loader").css("display", "none");
        $(window).trigger('resize');
    });

});
//---------Page Loader---------//

//PopUp
$(document).ready(function () {



       
    $('.popup-close-btn').click(function () {
        $('.popup-body-container').addClass("hide").removeClass("popup-active");
    });

});
//PopUp

$(document).on("click", '.menu-container .submenu-head', function () {
    $(this).parent("li").toggleClass("active");
});