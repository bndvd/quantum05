package bdn.quantum.contoller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import bdn.quantum.QuantumConstants;
import bdn.quantum.model.qplot.QPlot;
import bdn.quantum.service.QPlotService;

@RestController("plotController")
@RequestMapping(QuantumConstants.REST_URL_BASE)
public class QPlotController {

	@Autowired
	private QPlotService qPlotService;
	
	@RequestMapping(value = "/chart/{chartName}", method = RequestMethod.GET)
	public QPlot getPlot(@PathVariable(value="chartName") String chartName) {
		return qPlotService.getPlot(chartName);
	}

	public void plotDataChanged() {
		qPlotService.clear();
	}
	
}
