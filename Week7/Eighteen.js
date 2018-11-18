var fs = require('fs');
//var performance = require('performance');


function read_file(path_to_file, callback){
	fs.readFile(path_to_file, 'utf8', function(err, content){
		if(err) {
			console.log("Unable to read input file!");
			return;
		}
		callback(content.replace(/[^0-9a-zA-Z]/gi, ' ').toLowerCase().match(/\S+/g), frequencies);
	});
}

function remove_stop_words(word_list, callback){
	fs.readFile('../stop_words.txt', 'utf8', function(err, content){
		if (err){
			console.log("Unable to read stop words!");
			return
		}
		stop_words = content.split(',')
		for(i=0; i<26; i++)
			stop_words.push(String.fromCharCode('a'.charCodeAt(0)+i));
		callback(word_list.filter(function(val) { return !stop_words.includes(val)}), sort);
	});
}

function frequencies(word_list, callback){
	wf = {}
	for(i in word_list){
		word = word_list[i];
		wf[word] = wf[word] ? wf[word]+1 : 1;
	}
	callback(Object.keys(wf).map(function(key) { return [key, wf[key]]}), print_text);
}

function sort(word_freqs, callback){
	callback(word_freqs.sort(function(a,b){ return a[1] < b[1] ? 1 : (a[1] == b[1] ? 0 : -1)}));
}

function print_text(word_freqs, callback){
	for(i in word_freqs.slice(0, 25)){
		console.log(word_freqs[i][0] + " - " + word_freqs[i][1]);
	}
}

function profile(func){
	return function(arg1, arg2){
		var t0 = (new Date()).getTime();//performance.now();
		func(arg1, arg2);
		var t1 = (new Date()).getTime();//performance.now();
		console.log(func.name + " takes " + (t1-t0) + " milliseconds");
	}
}
//Tracked functions
read_file = profile(read_file)
frequencies = profile(frequencies)
print_text = profile(print_text)

read_file(process.argv[2], remove_stop_words)

