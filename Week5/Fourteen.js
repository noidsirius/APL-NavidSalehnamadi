var fs = require('fs');

class WordFrequencyFramework{
	constructor(){
		this.load_event_handlers = [];
		this.dowork_event_handlers = [];
		this.end_event_handlers = [];
	}
	register_for_load_event(handler){
		this.load_event_handlers.push(handler)
	}
	register_for_dowork_event(handler){
		this.dowork_event_handlers.push(handler)
	}
	register_for_end_event(handler){
		this.end_event_handlers.push(handler)
	}
	run(path_to_file){
		for(var i=0; i< this.load_event_handlers.length; i++){
			this.load_event_handlers[i](path_to_file);
		}
		for(var i=0; i< this.dowork_event_handlers.length; i++){
			this.dowork_event_handlers[i]();
		}
		for(var i=0; i< this.end_event_handlers.length; i++){
			this.end_event_handlers[i](path_to_file);
		}

	}	
}

function StopWordFilter(wfapp){
	this.stop_words = [];
	this.load = function(me){
		return function(ignore){
			var content = fs.readFileSync('../stop_words.txt', 'utf8');
			me.stop_words = content.split(',');
			for(var i=0; i<26; i++)
				me.stop_words.push(String.fromCharCode('a'.charCodeAt(0)+i));
		}};
	wfapp.register_for_load_event(this.load(this));

	this.is_stop_word = function(word){
		//console.log("StopWord",word);
	//	console.log(this.stop_words);
		return this.stop_words.includes(word);
	}
}

function DataStorage(wfapp, stop_word_filter){
	this.data = []
		this.stop_word_filter = stop_word_filter;
	this.load = function(me){ return function(path_to_file){
		content = fs.readFileSync(path_to_file, 'utf8');
		me.data = content.replace(/[^0-9a-zA-Z]/gi, ' ').toLowerCase().match(/\S+/g);
	};};
	this.produce_words = function(me) {return function(){
		//console.log("_-------");
		//console.log(me.data[4]);
		for(var j in me.data){
			var word = me.data[j];
			if(!me.stop_word_filter.is_stop_word(word))
				for(var i=0; i<me.word_event_handlers.length; i++)
					me.word_event_handlers[i](word);
		}
	};};	
	wfapp.register_for_load_event(this.load(this));
	wfapp.register_for_dowork_event(this.produce_words(this));
	this.word_event_handlers = [];
	this.register_for_word_event = function(handler){
		this.word_event_handlers.push(handler);
	}
}

function WordFrequencyCounter(wfapp, data_storage){
	this.word_freqs = {};
	this.increment_count = function(me){ return function(word){
		me.word_freqs[word] = me.word_freqs[word] ? me.word_freqs[word]+1 : 1;
	};};
	this.print_freqs = function(me){ return function(){
//		console.log("G");
//		console.log(me.word_freqs);
		//return;
		wf_list = Object.keys(me.word_freqs).map(function(key) { return [key, me.word_freqs[key]]})
			wfs = wf_list.sort(function(a,b){ return a[1] < b[1] ? 1 : (a[1] == b[1] ? 0 : -1)});
		for(i in wfs.slice(0, 25)){
			console.log(wfs[i][0] + " - " + wfs[i][1]);
		}
	};};
	data_storage.register_for_word_event(this.increment_count(this));
	wfapp.register_for_end_event(this.print_freqs(this));
}

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

//read_file(process.argv[2], remove_stop_words);
framework = new WordFrequencyFramework();
stop_word_filter = new StopWordFilter(framework);
data_storage = new DataStorage(framework, stop_word_filter);
word_freq_counter = new WordFrequencyCounter(framework, data_storage);
//framework.load_event_handlers[0](process.argv[2]);
//console.log(stop_word_filter.stop_words.length);
//stop_word_filter.load();
//console.log(stop_word_filter.stop_words);
framework.run(process.argv[2]);


