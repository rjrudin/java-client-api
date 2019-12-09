'use strict';

const state = fn.head(xdmp.fromJSON(endpointState));
const work = fn.head(xdmp.fromJSON(workUnit));

if (state.next > work.max) {
	null;
} else {
	const d = fn.subsequence(
		cts.search(cts.collectionQuery("bulkOutputTest"),
			cts.indexOrder(cts.uriReference())),
		state.next, 1
	);
	state.next = state.next + 1;
	Sequence.from([state, d]);
}
