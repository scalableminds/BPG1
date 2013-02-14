$(function() {
	var paper = Raphael('draw', 30, '100%', '100%');
	for (var i = 0; i < 20; ++i) {	
		var random_w = Math.floor(Math.random()*500);
		var random_h = Math.floor(Math.random()*500);
		window['im_' + i] = "http://placehold.it/" + random_w + "x" + random_h;	
	}	
	
	paper.setStart();
	for (var i = 0; i < 1000; ++i) {
		var random_x = Math.floor(Math.random()*1001);
		var random_y = Math.floor(Math.random()*1001);
		paper.image(eval('im_' + Math.floor(Math.random()*20)), random_x, random_y, 50, 50).id = i;
	}
	images = paper.setFinish();
	
	paper.setStart();
	for (var i = 0; i < 10000; ++i) {
		var random_x = Math.floor(Math.random()*2001);
		var random_y = Math.floor(Math.random()*2001);
		var c = paper.circle(random_x, random_y, 30);
		c.attr({
			fill: 'steelblue',
		});
		c.id = i + 10000
	}
	circles = paper.setFinish();
	
	paper.setStart();
	for (var i = 0; i < 10000; ++i) {
		var c = paper.getById(i + 10000);
		var x = c.attr("cx");
		var y = c.attr("cy");
		paper.text(x, y, "blabla").id = i + 2000;
	}
	texts = paper.setFinish();
	
	var start = function () {
		this.ox = this.attr("x");
		this.oy = this.attr("y");
		this.animate({opacity: .5}, 500, ">");
	},
	move = function (dx, dy) {
		this.attr({x: this.ox + dx, y: this.oy + dy});
	},
	up = function () {
		this.animate({opacity: 1.}, 500, ">");
	};

	var start_c = function () {
		this.ox = this.attr("cx");
		this.oy = this.attr("cy");
		this.animate({opacity: .5}, 500, ">");
	},
	move_c = function (dx, dy) {
		this.attr({cx: this.ox + dx, cy: this.oy + dy});
		var t = paper.getById(this.id - 8000);
		t.attr({x: this.ox + dx, y: this.oy + dy});
	},
	up_c = function () {
		this.animate({opacity: 1.}, 500, ">");
	};
	
	images.drag(move, start, up);
	circles.drag(move_c, start_c, up_c);
});