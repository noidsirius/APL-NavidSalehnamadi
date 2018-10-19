#!/usr/bin/env python
import re, sys, operator

# Mileage may vary. If this crashes, make it lower
RECURSION_LIMIT = 9500
# We add a few more, because, contrary to the name,
# this doesn't just rule recursion: it rules the 
# depth of the call stack
sys.setrecursionlimit(RECURSION_LIMIT)


stop_words = set(open('../stop_words.txt').read().split(','))
words = re.findall('[a-z]{2,}', open(sys.argv[1]).read().lower())
word_freqs = {}


# Navid Salehnamadi's Recursive functions	
Y = lambda f: (lambda x: x(x))(lambda y: f(lambda *args: y(y)(*args)))

count_Y = lambda f: lambda word_list,  stopwords, word_freqs: word_freqs if word_list == [] else (f(word_list[1:], stopwords, word_freqs) if word_list[0] in stopwords else ((f(word_list[1:], stopwords, word_freqs) if not word_freqs.update({word_list[0]: word_freqs[word_list[0]]+1}) else None) if word_list[0] in word_freqs else f(word_list[1:], stopwords, word_freqs) if not word_freqs.update({word_list[0]: 1}) else None))

wf_print_Y = lambda f: lambda wordfreq: None if wordfreq == [] else (f(wordfreq[1:]) if not print(wordfreq[0][0],'-',wordfreq[0][1]) else None)
# End of Navid's functions

# Using these functions
# Since in this version we use Y Combinator, it will call more functions than a regular python functions, so I changed the MAX_REC_LIMIT
MAX_REC_LIMIT = 200
for i in range(0, len(words), MAX_REC_LIMIT):
    Y(count_Y)(words[i:i+MAX_REC_LIMIT], stop_words, word_freqs)

Y(wf_print_Y)(sorted(word_freqs.items(), key=operator.itemgetter(1), reverse=True)[:25])

