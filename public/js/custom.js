(function ($) {

  	"use strict";

    /*==== Loader ====*/
    $('.preloader').fadeOut(1000); // set duration in brackets


    /*==== Nav ====*/
    $('.navbar-collapse a').on('click',function(){
    	$(".navbar-collapse").collapse('hide');
    });	
    
	/*==== Sticky ====*/
	$("#header").sticky({topSpacing:0});
	

    /*==== Counter ====*/
    $('.counter-item').appear(function() {
    	$('.counter-number').countTo();
    });
	
	//Text Typer
	var TxtType = function(el, toRotate, period) {
        this.toRotate = toRotate;
        this.el = el;
        this.loopNum = 0;
        this.period = parseInt(period, 10) || 2000;
        this.txt = '';
        this.tick();
        this.isDeleting = false;
    };

    TxtType.prototype.tick = function() {
        var i = this.loopNum % this.toRotate.length;
        var fullTxt = this.toRotate[i];

        if (this.isDeleting) {
        this.txt = fullTxt.substring(0, this.txt.length - 1);
        } else {
        this.txt = fullTxt.substring(0, this.txt.length + 1);
        }

        this.el.innerHTML = '<span class="wrap">'+this.txt+'</span>';

        var that = this;
        var delta = 200 - Math.random() * 100;

        if (this.isDeleting) { delta /= 2; }

        if (!this.isDeleting && this.txt === fullTxt) {
        delta = this.period;
        this.isDeleting = true;
        } else if (this.isDeleting && this.txt === '') {
        this.isDeleting = false;
        this.loopNum++;
        delta = 500;
        }

        setTimeout(function() {
        that.tick();
        }, delta);
    };

    window.onload = function() {
        var elements = document.getElementsByClassName('typewrite');
        for (var i=0; i<elements.length; i++) {
            var toRotate = elements[i].getAttribute('data-type');
            var period = elements[i].getAttribute('data-period');
            if (toRotate) {
              new TxtType(elements[i], JSON.parse(toRotate), period);
            }
        }
        // INJECT CSS
        var css = document.createElement("style");
        css.type = "text/css";
        css.innerHTML = ".typewrite > .wrap { border-right: 0.08em solid #fff}";
        document.body.appendChild(css);
    };
	
   	
	/*==== Testimonials ====*/
	$(document).ready(function() { 
	$(".testimonialsList").owlCarousel({ 		  
	   loop:true,
		margin:0,
		nav:false,
		responsiveClass:true,
		responsive:{
			0:{
				items:1,
				nav:false,
				loop:true
			},
			700:{
				items:1,
				nav:false,
				loop:true
			},
			1170:{
				items:1,
				nav:true,
				loop:true
			}
		}
	  
	  
	}); 
	});
	
	
	/*==== Blog ====*/
	$(document).ready(function() { 
	$(".blogGrid").owlCarousel({ 		  
	   loop:true,
		margin:30,
		nav:false,
		responsiveClass:true,
		responsive:{
			0:{
				items:1,
				nav:false,
				loop:true
			},
			700:{
				items:2,
				nav:false,
				loop:true
			},
			1170:{
				items:3,
				nav:true,
				loop:true
			}
		}
	  
	  
	}); 
	});
		
		
	/*==== Clients Logo ====*/
	$(document).ready(function() { 
	$(".owl-clients").owlCarousel({ 		  
	   loop:true,
		margin:30,
		nav:false,
		responsiveClass:true,
		responsive:{
			0:{
				items:2,
				nav:false,
				loop:true
			},
			700:{
				items:4,
				nav:false,
				loop:true
			},
			1170:{
				items:5,
				nav:true,
				loop:true
			}
		}
	  
	  
	}); 
	});
	

    /*==== Smoothscroll ====*/    
	$('#home a, .custom-navbar a').on('click', function(event) {
		var $anchor = $(this);
		  $('html, body').stop().animate({
			scrollTop: $($anchor.attr('href')).offset().top - 49
		}, 1000);
		  event.preventDefault();
	});
	
	/* ==== Revolution Slider ==== */
	if($('.tp-banner').length > 0){
		$('.tp-banner').show();
	}
    $(document).ready(function(){
        if (!localStorage.getItem("tnc")) {
            $(".modal").fadeIn();
            $(".modal_main").show();
        }

    });
    $(document).ready(function(){
        $(".modalClose1").click(function(){
            localStorage.setItem("tnc", "yes");
            $(".modal").fadeOut();
            $(".modal_main").fadeOut();
        });
    });

    $(document).ready(function(){
        $(".modalClose2").click(function(){
            window.location.replace("http://www.mediaflix.com");
        });
    });


})(jQuery);