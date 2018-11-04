var fs = require('fs');

function EventManager(){
	this.subscriptions = {};
	this.subscribe = function(event_type, handler){
		if(this.subscriptions[event_type])
			this.subscriptions[event_type].push(handler);
		else
			this.subscriptions[event_type] = [handler];
	}
	this.publish = function(eve){
		event_type = eve[0];
		if(this.subscriptions[event_type]){
			for(i in this.subscriptions[event_type])
			{
				this.subscriptions[event_type][i](eve);
			}
		}
	}
}

function StopWordFilter(event_manager){
	this.stop_words = [];
	this.event_manager = event_manager;
	this.load = function(me){
		return function(ignore){
			var content = fs.readFileSync('../stop_words.txt', 'utf8');
			me.stop_words = content.split(',');
			for(var i=0; i<26; i++)
				me.stop_words.push(String.fromCharCode('a'.charCodeAt(0)+i));
		}};
	this.is_stop_word = function(me){ return function(eve){
		var word = eve[1];
		if(!me.stop_words.includes(word))
			me.event_manager.publish(['valid_word',word]);
	};};
	this.event_manager.subscribe('load', this.load(this));
	this.event_manager.subscribe('word', this.is_stop_word(this));
}

function DataStorage(event_manager){
	this.data = []
	this.event_manager = event_manager;
	this.load = function(me){ return function(eve){
		var path_to_file = eve[1];
		content = fs.readFileSync(path_to_file, 'utf8');
		me.data = content.replace(/[^0-9a-zA-Z]/gi, ' ').toLowerCase().match(/\S+/g);
	};};
	this.produce_words = function(me) {return function(eve){
		for(var j in me.data){
			var word = me.data[j];
			me.event_manager.publish(['word', word]);
		}
		me.event_manager.publish(['print']);
	};};
	this.event_manager.subscribe('load', this.load(this));
	this.event_manager.subscribe('start', this.produce_words(this));
}

function WordFrequencyCounter(event_manager){
	this.word_freqs = {};
	this.event_manager = event_manager;
	this.increment_count = function(me){ return function(eve){
		var word = eve[1];
		me.word_freqs[word] = me.word_freqs[word] ? me.word_freqs[word]+1 : 1;
	};};
	this.print_freqs = function(me){ return function(eve){
		wf_list = Object.keys(me.word_freqs).map(function(key) { return [key, me.word_freqs[key]]})
			wfs = wf_list.sort(function(a,b){ return a[1] < b[1] ? 1 : (a[1] == b[1] ? 0 : -1)});
		for(i in wfs.slice(0, 25)){
			console.log(wfs[i][0] + " - " + wfs[i][1]);
		}
	};};
	this.event_manager.subscribe('valid_word', this.increment_count(this));
	this.event_manager.subscribe('print', this.print_freqs(this));
}

function WordZCounter(event_manager){
	this.word_freqs = {};
	this.event_manager = event_manager;
	this.increment_count = function(me){ return function(eve){
		var word = eve[1];
		if(word.includes('z'))
			me.word_freqs[word] = me.word_freqs[word] ? me.word_freqs[word]+1 : 1;
	};};
	this.print_freqs = function(me){ return function(eve){
		wf_list = Object.keys(me.word_freqs).map(function(key) { return [key, me.word_freqs[key]]});
		var z_distinct = 0;
		var z_total = 0;
		for(i in wf_list){
			if(wf_list[i][0].includes('z')){
				z_distinct += 1;
				z_total += wf_list[i][1];
			}
		}
		console.log("Z distinct",z_distinct);
		console.log("Z total",z_total);
	};};
	this.event_manager.subscribe('valid_word', this.increment_count(this));
	this.event_manager.subscribe('print', this.print_freqs(this));
}
	
function WordFrequencyApplication(event_manager){
	this.event_manager = event_manager;
	this.run = function(me){ return function(eve){
		path_to_file = eve[1];
		me.event_manager.publish(['load', path_to_file]);
		me.event_manager.publish(['start', null]);
	};};
	this.event_manager.subscribe('run', this.run(this));
}

em = new EventManager();
ds = new DataStorage(em);
swf = new StopWordFilter(em);
wfc = new WordFrequencyCounter(em);
wzc = new WordZCounter(em);
wfapp = new WordFrequencyApplication(em);
em.publish(['run', process.argv[2]]);

