<script setup>
	import PaymentFormImage from "./components/PaymentFormImage.vue";
	import { OrbitSpinner } from "epic-spinners";
	import { ref } from "vue";

	const formLoading = ref(false);
	const mobileNumber = ref("");
	const paymentAmount = ref("");
	const responseMessage = ref("");

	const handleFormSubmission = async () => {
		formLoading.value = true;

		const requestBody = {
			number: mobileNumber.value,
			amount: paymentAmount.value,
		};
		await fetch("/api/v1/payment/initiate", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
			},
			body: JSON.stringify(requestBody),
		})
			.then((response) => {
				if (response.status !== 200) {
					console.error(
						"Something went wrong. Please try again later."
					);
					formLoading.value = false;
					return response.json();
				}

				formLoading.value = false;
				return response.json();
			})
			.then((data) => {
				console.log(`Response data: ${JSON.stringify(data)}`);
				responseMessage.value = data.message;
			});
	};
</script>

<template>
	<main class="flex flex-col h-screen justify-center items-center">
		<form
			class="rounded-md p-4 w-[35%] flex justify-center items-center flex-col h-fit"
			@submit.prevent="handleFormSubmission">
			<PaymentFormImage />
			<h1 class="text-2xl font-semibold">STK Payment Request</h1>
			<input
				type="text"
				placeholder="mobile number e.g. 254..."
				class="input-fields"
				name="mobile-number"
				v-model="mobileNumber" />
			<input
				type="text"
				placeholder="amount"
				class="input-fields"
				name="amount"
				v-model="paymentAmount" />
			<div class="flex justify-center w-full">
				<button
					class="bg-purple-400 hover:bg-purple-600 transition-colors duration-200 ease-linear p-2 text-white rounded-md w-1/2 inline-flex justify-center items-center"
					type="submit">
					<span v-if="!formLoading">Purchase</span>
					<orbit-spinner
						v-else
						:animation-duration="1200"
						:size="28"
						color="#ffffff" />
				</button>
			</div>
		</form>
		<span
			v-if="responseMessage"
			class="text-gray-500 font-semibold text-lg"
			>{{ responseMessage }}</span
		>
	</main>
</template>
