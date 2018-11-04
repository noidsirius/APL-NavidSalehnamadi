var fs = require('fs');

function TFQuarantine(func){
	this.funcs = [func];
	this.bind = function(func){
		this.funcs.push(func);
		return this;
	}
	this.execute = function(){
		var guard_callable = function(v){
			if(typeof v == 'function')
			  	return v();
			return v;
		}
		var value = null;
		for(i in this.funcs){
			value = this.funcs[i](guard_callable(value));
		}
		guard_callable(value);

	}
}

function get_input(arg){
	var f = function(){
		return process.argv[2];
	}
	return f;
}

function read_file(path_to_file){
	var f = function(){
		var content = fs.readFileSync(path_to_file, 'utf8')
		return content.replace(/[^0-9a-zA-Z]/gi, ' ').toLowerCase().match(/\S+/g);
	};
	return f;
}

function remove_stop_words(word_list){
	var f = function(){
		var content = fs.readFileSync('../stop_words.txt', 'utf8')
		stop_words = content.split(',')
		for(i=0; i<26; i++)
			stop_words.push(String.fromCharCode('a'.charCodeAt(0)+i));
		return word_list.filter(function(val) { return !stop_words.includes(val)});
	}
	return f;
}

function frequencies(word_list){
	wf = {}
	for(i in word_list){
		word = word_list[i];
		wf[word] = wf[word] ? wf[word]+1 : 1;
	}
	return Object.keys(wf).map(function(key) { return [key, wf[key]]});
}

function sort(word_freqs){
	return word_freqs.sort(function(a,b){ return a[1] < b[1] ? 1 : (a[1] == b[1] ? 0 : -1)});
}

function print_text(word_freqs){
	var f = function(){
		for(i in word_freqs.slice(0, 25)){
			console.log(word_freqs[i][0] + " - " + word_freqs[i][1]);
		}
	};
	return f;
}

var quarantine = new TFQuarantine(get_input).bind(read_file).bind(remove_stop_words).bind(frequencies).bind(sort).bind(print_text);
quarantine.execute();
//read_file(process.argv[2], remove_stop_words)
